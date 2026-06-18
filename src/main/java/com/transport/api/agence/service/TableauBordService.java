package com.transport.api.agence.service;

import com.transport.api.agence.dto.TableauBordAgenceDto;
import com.transport.api.bus.entity.Bus;
import com.transport.api.bus.repository.BusRepository;
import com.transport.api.paiement.entity.Transaction;
import com.transport.api.paiement.repository.TransactionRepository;
import com.transport.api.reservation.entity.Reservation;
import com.transport.api.reservation.repository.ReservationRepository;
import com.transport.api.trajet.entity.Ligne;
import com.transport.api.trajet.entity.Trajet;
import com.transport.api.trajet.repository.LigneRepository;
import com.transport.api.trajet.repository.TrajetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TableauBordService {

    private final ReservationRepository reservationRepository;
    private final TransactionRepository transactionRepository;
    private final TrajetRepository trajetRepository;
    private final BusRepository busRepository;
    private final LigneRepository ligneRepository;  // ← AJOUT

    public TableauBordAgenceDto getTableauBord(Long agenceId) {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = LocalDate.now().atTime(LocalTime.MAX);

        // Ventes du jour
        List<Transaction> transactionsDuJour = transactionRepository.findByAgenceIdAndDateBetween(agenceId, todayStart, todayEnd);
        Integer ventesDuJour = transactionsDuJour.size();
        Double montantTotalDuJour = transactionsDuJour.stream()
                .mapToDouble(t -> t.getAmount().doubleValue())
                .sum();

        // Réservations du jour - CORRIGÉ
        List<Reservation> reservationsDuJourList = reservationRepository.findByAgenceIdAndCreatedAtBetween(agenceId, todayStart, todayEnd);
        Integer reservationsDuJour = reservationsDuJourList.size();
        Integer passagersDuJour = reservationsDuJourList.stream()
                .mapToInt(r -> r.getNumberOfPassengers())
                .sum();

        // Prochains départs (dans les 24h)
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime tomorrow = now.plusHours(24);
        List<Trajet> prochainsTrajets = trajetRepository.findByCompanyIdAndDepartureTimeBetween(1L, now, tomorrow);

        List<TableauBordAgenceDto.ProchainDepartDto> prochainsDeparts = prochainsTrajets.stream()
                .limit(10)
                .map(t -> {
                    Bus bus = busRepository.findById(t.getBusId()).orElse(null);
                    Ligne ligne = ligneRepository.findById(t.getLigneId()).orElse(null);  // ← CORRIGÉ

                    Integer totalPlaces = bus != null ? bus.getCapacity() : 0;
                    Integer placesReservees = reservationRepository.countConfirmedReservationsByTrajetId(t.getId()).intValue();
                    Integer placesDisponibles = totalPlaces - placesReservees;
                    Double tauxRemplissage = totalPlaces > 0 ? (placesReservees * 100.0 / totalPlaces) : 0.0;

                    return TableauBordAgenceDto.ProchainDepartDto.builder()
                            .trajetId(t.getId())
                            .departureCity(ligne != null ? ligne.getDepartureCity() : "Inconnu")  // ← CORRIGÉ
                            .arrivalCity(ligne != null ? ligne.getArrivalCity() : "Inconnu")      // ← CORRIGÉ
                            .departureTime(t.getDepartureTime())
                            .placesDisponibles(placesDisponibles)
                            .totalPlaces(totalPlaces)
                            .tauxRemplissage(tauxRemplissage)
                            .build();
                })
                .collect(Collectors.toList());

        // Statistiques mensuelles
        LocalDateTime monthStart = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        List<Transaction> transactionsMois = transactionRepository.findByAgenceIdAndDateBetween(agenceId, monthStart, todayEnd);
        Integer totalVentesMois = transactionsMois.size();
        Double montantTotalMois = transactionsMois.stream()
                .mapToDouble(t -> t.getAmount().doubleValue())
                .sum();

        // Statistiques hebdomadaires
        LocalDateTime weekStart = LocalDate.now().minusDays(7).atStartOfDay();
        List<Transaction> transactionsSemaine = transactionRepository.findByAgenceIdAndDateBetween(agenceId, weekStart, todayEnd);
        Integer totalVentesSemaine = transactionsSemaine.size();
        Double montantTotalSemaine = transactionsSemaine.stream()
                .mapToDouble(t -> t.getAmount().doubleValue())
                .sum();

        // Taux de remplissage moyen
        Double tauxRemplissageMoyen = prochainsTrajets.stream()
                .mapToDouble(t -> {
                    Bus bus = busRepository.findById(t.getBusId()).orElse(null);
                    Integer totalPlaces = bus != null ? bus.getCapacity() : 0;
                    Integer placesReservees = reservationRepository.countConfirmedReservationsByTrajetId(t.getId()).intValue();
                    return totalPlaces > 0 ? (placesReservees * 100.0 / totalPlaces) : 0.0;
                })
                .average()
                .orElse(0.0);

        return TableauBordAgenceDto.builder()
                .agenceId(agenceId)
                .date(LocalDateTime.now())
                .ventesDuJour(ventesDuJour)
                .montantTotalDuJour(montantTotalDuJour)
                .reservationsDuJour(reservationsDuJour)
                .passagersDuJour(passagersDuJour)
                .tauxRemplissageMoyen(tauxRemplissageMoyen)
                .prochainsDeparts(prochainsDeparts)
                .resumes(TableauBordAgenceDto.ResumesDto.builder()
                        .totalVentesMois(totalVentesMois)
                        .montantTotalMois(montantTotalMois)
                        .totalVentesSemaine(totalVentesSemaine)
                        .montantTotalSemaine(montantTotalSemaine)
                        .build())
                .build();
    }
}