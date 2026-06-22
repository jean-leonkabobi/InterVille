package com.transport.api.admin.service;

import com.transport.api.admin.dto.*;
import com.transport.api.agence.entity.Agence;
import com.transport.api.agence.repository.AgenceRepository;
import com.transport.api.bus.entity.Bus;
import com.transport.api.bus.repository.BusRepository;
import com.transport.api.paiement.entity.Transaction;
import com.transport.api.paiement.enums.StatutTransaction;
import com.transport.api.paiement.repository.TransactionRepository;
import com.transport.api.reservation.entity.Reservation;
import com.transport.api.reservation.entity.ReservationSiege;
import com.transport.api.reservation.repository.ReservationRepository;
import com.transport.api.reservation.repository.ReservationSiegeRepository;
import com.transport.api.trajet.entity.Ligne;
import com.transport.api.trajet.entity.Trajet;
import com.transport.api.trajet.repository.LigneRepository;
import com.transport.api.trajet.repository.TrajetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RapportsService {

    private final TransactionRepository transactionRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationSiegeRepository reservationSiegeRepository;
    private final AgenceRepository agenceRepository;
    private final LigneRepository ligneRepository;
    private final TrajetRepository trajetRepository;
    private final BusRepository busRepository;

    private static final String DEVISE = "CDF";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * FG11 - Rapport général des ventes par période
     */
    public RapportVentesDto getRapportVentes(RapportPeriodeRequest request) {
        LocalDateTime start = request.getDateDebut().atStartOfDay();
        LocalDateTime end = request.getDateFin().atTime(LocalTime.MAX);

        List<Transaction> transactions = transactionRepository.findByPaymentDateBetweenAndStatus(start, end, StatutTransaction.SUCCESS);

        Long totalReservations = transactions.stream()
                .map(Transaction::getReservationId)
                .distinct()
                .count();

        Long totalPassagers = reservationRepository.countPassagersByTransactions(transactions);

        Double montantTotal = transactions.stream()
                .mapToDouble(t -> t.getAmount().doubleValue())
                .sum();

        Double montantMoyen = transactions.isEmpty() ? 0.0 : montantTotal / transactions.size();
        Double montantMin = transactions.stream()
                .mapToDouble(t -> t.getAmount().doubleValue())
                .min()
                .orElse(0.0);
        Double montantMax = transactions.stream()
                .mapToDouble(t -> t.getAmount().doubleValue())
                .max()
                .orElse(0.0);

        String periode = request.getDateDebut().format(DATE_FORMATTER) + " - " + request.getDateFin().format(DATE_FORMATTER);

        return RapportVentesDto.builder()
                .periode(periode)
                .dateDebut(start)
                .dateFin(end)
                .totalReservations(totalReservations)
                .totalPassagers(totalPassagers)
                .montantTotal(montantTotal)
                .montantMoyen(montantMoyen)
                .montantMin(montantMin)
                .montantMax(montantMax)
                .devise(DEVISE)
                .build();
    }

    /**
     * FG11 - Rapport des ventes par agence
     */
    public List<RapportVentesAgenceDto> getRapportVentesParAgence(RapportPeriodeRequest request) {
        LocalDateTime start = request.getDateDebut().atStartOfDay();
        LocalDateTime end = request.getDateFin().atTime(LocalTime.MAX);

        List<Agence> agences = agenceRepository.findAll();
        List<Transaction> transactions = transactionRepository.findByPaymentDateBetweenAndStatus(start, end, StatutTransaction.SUCCESS);

        return agences.stream()
                .map(agence -> {
                    List<Transaction> agenceTransactions = transactions.stream()
                            .filter(t -> t.getAgenceId() != null && t.getAgenceId().equals(agence.getId()))
                            .collect(Collectors.toList());

                    Long totalReservations = agenceTransactions.stream()
                            .map(Transaction::getReservationId)
                            .distinct()
                            .count();

                    Double montantTotal = agenceTransactions.stream()
                            .mapToDouble(t -> t.getAmount().doubleValue())
                            .sum();

                    Double montantMoyen = agenceTransactions.isEmpty() ? 0.0 : montantTotal / agenceTransactions.size();

                    return RapportVentesAgenceDto.builder()
                            .agenceId(agence.getId())
                            .agenceNom(agence.getName())
                            .totalReservations(totalReservations)
                            .totalPassagers(0L) // À calculer si nécessaire
                            .montantTotal(montantTotal)
                            .montantMoyen(montantMoyen)
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * FG11 - Rapport des ventes par ligne
     */
    public List<RapportVentesLigneDto> getRapportVentesParLigne(RapportPeriodeRequest request) {
        LocalDateTime start = request.getDateDebut().atStartOfDay();
        LocalDateTime end = request.getDateFin().atTime(LocalTime.MAX);

        List<Ligne> lignes = ligneRepository.findAll();
        List<Reservation> reservations = reservationRepository.findByCreatedAtBetweenAndStatus(start, end, "PAID");

        return lignes.stream()
                .map(ligne -> {
                    List<Reservation> ligneReservations = reservations.stream()
                            .filter(r -> {
                                Trajet trajet = trajetRepository.findById(r.getTrajetId()).orElse(null);
                                return trajet != null && trajet.getLigneId().equals(ligne.getId());
                            })
                            .collect(Collectors.toList());

                    Long totalReservations = (long) ligneReservations.size();
                    Double montantTotal = ligneReservations.stream()
                            .mapToDouble(r -> r.getTotalPrice().doubleValue())
                            .sum();
                    Double montantMoyen = ligneReservations.isEmpty() ? 0.0 : montantTotal / ligneReservations.size();

                    // Calcul du taux de remplissage
                    Double tauxRemplissage = calculerTauxRemplissage(ligne.getId(), start, end);

                    return RapportVentesLigneDto.builder()
                            .ligneId(ligne.getId())
                            .departureCity(ligne.getDepartureCity())
                            .arrivalCity(ligne.getArrivalCity())
                            .totalReservations(totalReservations)
                            .totalPassagers(0L)
                            .montantTotal(montantTotal)
                            .montantMoyen(montantMoyen)
                            .tauxRemplissage(tauxRemplissage)
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * Calcule le taux de remplissage moyen pour une ligne sur une période donnée
     */
    private Double calculerTauxRemplissage(Long ligneId, LocalDateTime start, LocalDateTime end) {
        // 1. Récupérer tous les trajets de cette ligne sur la période
        List<Trajet> trajets = trajetRepository.findByLigneIdAndDepartureTimeBetween(ligneId, start, end);

        if (trajets.isEmpty()) {
            return 0.0;
        }

        // 2. Pour chaque trajet, calculer son taux de remplissage
        double totalTaux = 0.0;
        int trajetsAvecDonnees = 0;

        for (Trajet trajet : trajets) {
            // Récupérer le bus
            Bus bus = busRepository.findById(trajet.getBusId()).orElse(null);
            if (bus == null) continue;

            Integer totalPlaces = bus.getCapacity();

            // Récupérer le nombre de places réservées pour ce trajet
            Long placesReservees = reservationRepository.countConfirmedReservationsByTrajetId(trajet.getId());

            if (totalPlaces > 0) {
                double taux = (placesReservees * 100.0) / totalPlaces;
                totalTaux += taux;
                trajetsAvecDonnees++;
            }
        }

        // 3. Retourner la moyenne
        return trajetsAvecDonnees > 0 ? totalTaux / trajetsAvecDonnees : 0.0;
    }
}