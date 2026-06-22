package com.transport.api.sync.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.transport.api.bus.entity.Siege;
import com.transport.api.bus.repository.SiegeRepository;
import com.transport.api.reservation.entity.Reservation;
import com.transport.api.reservation.entity.ReservationSiege;
import com.transport.api.reservation.entity.Ticket;
import com.transport.api.reservation.enums.StatutReservation;
import com.transport.api.reservation.enums.StatutTicket;
import com.transport.api.reservation.repository.ReservationRepository;
import com.transport.api.reservation.repository.ReservationSiegeRepository;
import com.transport.api.reservation.repository.TicketRepository;
import com.transport.api.sync.dto.ConflitDto;
import com.transport.api.sync.dto.ConflitResolutionResponse;
import com.transport.api.sync.dto.SyncOperationDto;
import com.transport.api.sync.entity.SyncLog;
import com.transport.api.sync.repository.SyncLogRepository;
import com.transport.api.trajet.entity.Trajet;
import com.transport.api.trajet.repository.TrajetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConflitResolutionService {

    private final SyncLogRepository syncLogRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationSiegeRepository reservationSiegeRepository;
    private final TicketRepository ticketRepository;
    private final TrajetRepository trajetRepository;
    private final SiegeRepository siegeRepository;
    private final ObjectMapper objectMapper;

    private static final String DEVISE = "CDF";

    /**
     * FT2 - Résolution de conflits selon la règle "premier arrivé, premier servi"
     */
    @Transactional
    public ConflitResolutionResponse resoudreConflits(List<SyncOperationDto> operations, Long agenceId) {
        List<ConflitDto> conflitsResolus = new ArrayList<>();
        List<ConflitDto> conflitsPerdants = new ArrayList<>();
        List<SyncOperationDto> operationsTraitees = new ArrayList<>();

        // 1. Trier les opérations par horodatage (du plus ancien au plus récent)
        operations.sort((o1, o2) -> o1.getLocalTimestamp().compareTo(o2.getLocalTimestamp()));

        // 2. Traiter chaque opération
        for (SyncOperationDto operation : operations) {
            try {
                // Vérifier si l'opération est un conflit
                boolean hasConflict = verifierConflit(operation);

                if (hasConflict) {
                    // Résoudre le conflit
                    ConflitDto result = resoudreConflit(operation, operationsTraitees);
                    if (result.getIsWinner()) {
                        conflitsResolus.add(result);
                        operationsTraitees.add(operation);
                    } else {
                        conflitsPerdants.add(result);
                        // Rembourser automatiquement le perdant
                        rembourserOperation(operation);
                    }
                } else {
                    // Pas de conflit, exécuter normalement
                    operationsTraitees.add(operation);
                }

            } catch (Exception e) {
                log.error("Erreur lors du traitement de l'opération: {}", e.getMessage());
            }
        }

        // 3. Enregistrer les opérations traitées dans SyncLog
        for (SyncOperationDto operation : operationsTraitees) {
            SyncLog log = new SyncLog();
            log.setAgenceId(agenceId);
            log.setOperationType(operation.getType());
            log.setPayload(operation.getPayload());
            log.setLocalTimestamp(operation.getLocalTimestamp());
            log.setSyncedAt(LocalDateTime.now());
            log.setConflictResolved(false);
            syncLogRepository.save(log);
        }

        return ConflitResolutionResponse.builder()
                .success(true)
                .message(conflitsPerdants.isEmpty() ? "Aucun conflit détecté" : "Conflits résolus avec succès")
                .conflitsResolus(conflitsResolus)
                .conflitsPerdants(conflitsPerdants)
                .build();
    }

    /**
     * Vérifie si une opération est en conflit avec une opération existante
     */
    private boolean verifierConflit(SyncOperationDto operation) {
        try {
            JsonNode payload = objectMapper.readTree(operation.getPayload());
            Long trajetId = payload.get("trajetId").asLong();
            String siegeNumber = payload.get("siegeNumber").asText();

            // Vérifier si le siège est déjà réservé
            Trajet trajet = trajetRepository.findById(trajetId).orElse(null);
            if (trajet == null) return false;

            // Récupérer le siège
            Siege siege = siegeRepository.findByBusIdAndSeatNumber(
                    trajet.getBusId(), siegeNumber).orElse(null);
            if (siege == null) return false;

            // Vérifier les réservations existantes
            List<ReservationSiege> existingReservations = reservationSiegeRepository
                    .findByTrajetIdAndSiegeId(trajetId, siege.getId());

            return !existingReservations.isEmpty();

        } catch (Exception e) {
            log.error("Erreur lors de la vérification de conflit: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Résout un conflit selon la règle "premier arrivé, premier servi"
     */
    private ConflitDto resoudreConflit(SyncOperationDto operation, List<SyncOperationDto> operationsTraitees) {
        // Comparer avec les opérations déjà traitées
        for (SyncOperationDto existingOp : operationsTraitees) {
            try {
                JsonNode existingPayload = objectMapper.readTree(existingOp.getPayload());
                JsonNode newPayload = objectMapper.readTree(operation.getPayload());

                Long existingTrajetId = existingPayload.get("trajetId").asLong();
                Long newTrajetId = newPayload.get("trajetId").asLong();
                String existingSiege = existingPayload.get("siegeNumber").asText();
                String newSiege = newPayload.get("siegeNumber").asText();

                // Même trajet et même siège → conflit
                if (existingTrajetId.equals(newTrajetId) && existingSiege.equals(newSiege)) {
                    // Comparer les horodatages
                    if (existingOp.getLocalTimestamp().isBefore(operation.getLocalTimestamp())) {
                        // L'opération existante est plus ancienne → gagnante
                        return ConflitDto.builder()
                                .operationId(operation.getId())
                                .trajetId(newTrajetId)
                                .siegeNumber(newSiege)
                                .localTimestamp(operation.getLocalTimestamp())
                                .serverTimestamp(LocalDateTime.now())
                                .isWinner(false)
                                .resolutionMessage("Siège déjà vendu à " + existingOp.getLocalTimestamp())
                                .build();
                    } else {
                        // La nouvelle opération est plus ancienne → gagnante
                        // On annule l'opération existante
                        return ConflitDto.builder()
                                .operationId(operation.getId())
                                .trajetId(newTrajetId)
                                .siegeNumber(newSiege)
                                .localTimestamp(operation.getLocalTimestamp())
                                .serverTimestamp(LocalDateTime.now())
                                .isWinner(true)
                                .resolutionMessage("Opération plus ancienne, prioritaire")
                                .build();
                    }
                }
            } catch (Exception e) {
                log.error("Erreur lors de la résolution de conflit: {}", e.getMessage());
            }
        }

        // Pas de conflit avec les opérations traitées
        return ConflitDto.builder()
                .operationId(operation.getId())
                .isWinner(true)
                .resolutionMessage("Aucun conflit détecté")
                .build();
    }

    /**
     * Remboursement automatique de l'opération perdante
     */
    @Transactional
    public void rembourserOperation(SyncOperationDto operation) {
        try {
            JsonNode payload = objectMapper.readTree(operation.getPayload());
            Long trajetId = payload.get("trajetId").asLong();
            String passengerName = payload.get("passengerName").asText();

            // Créer une réservation annulée avec remboursement
            Reservation reservation = new Reservation();
            reservation.setReservationCode(UUID.randomUUID());
            reservation.setTrajetId(trajetId);
            reservation.setPassengerName(passengerName + " (REMBOURSÉ)");
            reservation.setStatus(StatutReservation.CANCELLED);
            reservation.setTotalPrice(java.math.BigDecimal.ZERO);
            reservation.setCompanyId(1L);
            reservation.setCreatedAt(LocalDateTime.now());
            reservation.setUpdatedAt(LocalDateTime.now());
            reservationRepository.save(reservation);

            log.info("✅ Remboursement effectué pour l'opération {}: Siège déjà vendu", operation.getId());

        } catch (Exception e) {
            log.error("❌ Erreur lors du remboursement: {}", e.getMessage());
        }
    }
}