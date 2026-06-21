package com.transport.api.admin.service;

import com.transport.api.admin.dto.TableauBordGlobalDto;
import com.transport.api.agence.repository.AgenceRepository;
import com.transport.api.bus.entity.Bus;
import com.transport.api.bus.repository.BusRepository;
import com.transport.api.incident.enums.StatutIncident;
import com.transport.api.incident.repository.IncidentRepository;
import com.transport.api.paiement.repository.TransactionRepository;
import com.transport.api.reservation.repository.ReservationRepository;
import com.transport.api.reservation.repository.ReservationSiegeRepository;
import com.transport.api.trajet.entity.Trajet;
import com.transport.api.trajet.repository.TrajetRepository;
import com.transport.api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TableauBordGlobalService {

    private final UserRepository userRepository;
    private final AgenceRepository agenceRepository;
    private final BusRepository busRepository;
    private final TrajetRepository trajetRepository;
    private final ReservationRepository reservationRepository;
    private final TransactionRepository transactionRepository;
    private final IncidentRepository incidentRepository;
    private final ReservationSiegeRepository reservationSiegeRepository;

    public TableauBordGlobalDto getTableauBordGlobal() {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = LocalDate.now().atTime(LocalTime.MAX);
        LocalDateTime monthStart = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime yearStart = LocalDate.now().withDayOfYear(1).atStartOfDay();

        // 1. Statistiques globales
        Long totalUtilisateurs = userRepository.count();
        Long totalAgences = agenceRepository.count();
        Long totalBus = busRepository.count();
        Long totalTrajets = trajetRepository.count();

        // 2. Revenus
        Double revenusTotal = transactionRepository.sumAllSuccessAmounts();
        Double revenusAujourdhui = transactionRepository.sumSuccessAmountsByDate(todayStart, todayEnd);
        Double revenusMois = transactionRepository.sumSuccessAmountsByDate(monthStart, todayEnd);
        Double revenusAnnee = transactionRepository.sumSuccessAmountsByDate(yearStart, todayEnd);

        // 3. Réservations
        Long reservationsTotal = reservationRepository.count();
        Long reservationsAujourdhui = reservationRepository.countByCreatedAtBetween(todayStart, todayEnd);
        Long reservationsMois = reservationRepository.countByCreatedAtBetween(monthStart, todayEnd);
        Long reservationsAnnee = reservationRepository.countByCreatedAtBetween(yearStart, todayEnd);

        // 4. Taux de remplissage moyen - CALCUL DÉPLACÉ DANS UNE MÉTHODE
        Double tauxRemplissageMoyen = calculerTauxRemplissageMoyen();

        // 5. Incidents
        Long incidentsOpen = incidentRepository.countByStatus(StatutIncident.OPEN);
        Long incidentsInProgress = incidentRepository.countByStatus(StatutIncident.IN_PROGRESS);
        Long incidentsResolved = incidentRepository.countByStatus(StatutIncident.RESOLVED);
        Long incidentsTotal = incidentRepository.count();

        // 6. Top agences
        List<TableauBordGlobalDto.TopAgenceDto> topAgences = getTopAgences();

        // 7. Top lignes
        List<TableauBordGlobalDto.TopLigneDto> topLignes = getTopLignes();

        return TableauBordGlobalDto.builder()
                .date(LocalDateTime.now())
                .totalUtilisateurs(totalUtilisateurs)
                .totalAgences(totalAgences)
                .totalBus(totalBus)
                .totalTrajets(totalTrajets)
                .revenusTotal(revenusTotal != null ? revenusTotal : 0.0)
                .revenusAujourdhui(revenusAujourdhui != null ? revenusAujourdhui : 0.0)
                .revenusMois(revenusMois != null ? revenusMois : 0.0)
                .revenusAnnee(revenusAnnee != null ? revenusAnnee : 0.0)
                .reservationsTotal(reservationsTotal)
                .reservationsAujourdhui(reservationsAujourdhui)
                .reservationsMois(reservationsMois)
                .reservationsAnnee(reservationsAnnee)
                .tauxRemplissageMoyen(tauxRemplissageMoyen != null ? tauxRemplissageMoyen : 0.0)
                .incidentsOpen(incidentsOpen)
                .incidentsInProgress(incidentsInProgress)
                .incidentsResolved(incidentsResolved)
                .incidentsTotal(incidentsTotal)
                .topAgences(topAgences)
                .topLignes(topLignes)
                .build();
    }

    /**
     * Calcule le taux de remplissage moyen des trajets
     */
    private Double calculerTauxRemplissageMoyen() {
        List<Trajet> trajets = trajetRepository.findAll();

        if (trajets == null || trajets.isEmpty()) {
            return 0.0;
        }

        double totalRemplissage = 0.0;
        int trajetsAvecBus = 0;

        for (Trajet trajet : trajets) {
            Bus bus = busRepository.findById(trajet.getBusId()).orElse(null);
            if (bus != null && bus.getCapacity() != null && bus.getCapacity() > 0) {
                Long reserved = reservationSiegeRepository.countReservedSeatsByTrajetId(trajet.getId());
                double taux = (reserved != null ? reserved : 0) * 100.0 / bus.getCapacity();
                totalRemplissage += taux;
                trajetsAvecBus++;
            }
        }

        return trajetsAvecBus > 0 ? totalRemplissage / trajetsAvecBus : 0.0;
    }

    private List<TableauBordGlobalDto.TopAgenceDto> getTopAgences() {
        // TODO: Implémenter la requête pour les top agences
        return List.of();
    }

    private List<TableauBordGlobalDto.TopLigneDto> getTopLignes() {
        // TODO: Implémenter la requête pour les top lignes
        return List.of();
    }
}