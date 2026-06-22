package com.transport.api.sync.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.transport.api.reservation.entity.Reservation;
import com.transport.api.reservation.service.ReservationService;
import com.transport.api.sync.dto.*;
import com.transport.api.sync.entity.SyncLog;
import com.transport.api.sync.enums.OperationType;
import com.transport.api.sync.repository.SyncLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SyncService {

    private final SyncLogRepository syncLogRepository;
    private final ReservationService reservationService;
    private final ConflitResolutionService conflitResolutionService;
    private final ObjectMapper objectMapper;

    /**
     * FA13 - Synchronisation des opérations hors ligne
     */
    @Transactional
    public SyncResponse synchroniser(SyncRequest request) {
        List<SyncResultDto> results = new ArrayList<>();

        for (SyncOperationDto operation : request.getOperations()) {
            try {
                SyncResultDto result = traiterOperation(operation);
                results.add(result);

                // Enregistrer dans le log de synchronisation
                SyncLog log = new SyncLog();
                log.setAgenceId(request.getAgenceId());
                log.setOperationType(operation.getType());
                log.setPayload(operation.getPayload());
                log.setLocalTimestamp(operation.getLocalTimestamp());
                log.setSyncedAt(LocalDateTime.now());
                log.setConflictResolved(result.getConflict() != null && result.getConflict());
                syncLogRepository.save(log);

            } catch (Exception e) {
                log.error("Erreur lors du traitement de l'opération: {}", e.getMessage());
                results.add(SyncResultDto.builder()
                        .operationId(operation.getId())
                        .success(false)
                        .message("Erreur: " + e.getMessage())
                        .build());
            }
        }

        return SyncResponse.builder()
                .success(true)
                .message(results.stream().allMatch(SyncResultDto::getSuccess) ? "Synchronisation complète" : "Synchronisation partielle")
                .results(results)
                .build();
    }

    private SyncResultDto traiterOperation(SyncOperationDto operation) {
        switch (operation.getType()) {
            case SALE:
                return traiterVente(operation);
            case CANCELLATION:
                return traiterAnnulation(operation);
            case REFUND:
                return traiterRemboursement(operation);
            default:
                return SyncResultDto.builder()
                        .operationId(operation.getId())
                        .success(false)
                        .message("Type d'opération inconnu: " + operation.getType())
                        .build();
        }
    }

    /**
     * Traitement d'une vente hors ligne
     */
    private SyncResultDto traiterVente(SyncOperationDto operation) {
        try {
            // Vérifier les conflits
            List<SyncOperationDto> operations = List.of(operation);
            ConflitResolutionResponse resolution = conflitResolutionService.resoudreConflits(operations, 1L);

            if (resolution.getConflitsPerdants().isEmpty()) {
                // Pas de conflit, la vente est validée
                return SyncResultDto.builder()
                        .operationId(operation.getId())
                        .success(true)
                        .message("Vente synchronisée avec succès")
                        .conflict(false)
                        .build();
            } else {
                // Conflit détecté, la vente est annulée
                return SyncResultDto.builder()
                        .operationId(operation.getId())
                        .success(false)
                        .message("Conflit détecté: " + resolution.getConflitsPerdants().get(0).getResolutionMessage())
                        .conflict(true)
                        .conflictResolution("Remboursement automatique effectué")
                        .build();
            }

        } catch (Exception e) {
            log.error("Erreur lors du traitement de la vente: {}", e.getMessage());
            return SyncResultDto.builder()
                    .operationId(operation.getId())
                    .success(false)
                    .message("Erreur lors de la synchronisation de la vente: " + e.getMessage())
                    .build();
        }
    }

    /**
     * Traitement d'une annulation hors ligne
     */
    private SyncResultDto traiterAnnulation(SyncOperationDto operation) {
        try {
            JsonNode payload = objectMapper.readTree(operation.getPayload());
            Long reservationId = payload.get("reservationId").asLong();
            String motif = payload.has("motif") ? payload.get("motif").asText() : "Annulation hors ligne";

            Reservation reservation = reservationService.annulerReservationSync(reservationId, motif);

            return SyncResultDto.builder()
                    .operationId(operation.getId())
                    .success(true)
                    .message("Annulation synchronisée avec succès. Réservation: " + reservation.getReservationCode())
                    .conflict(false)
                    .build();

        } catch (Exception e) {
            log.error("Erreur lors du traitement de l'annulation: {}", e.getMessage());
            return SyncResultDto.builder()
                    .operationId(operation.getId())
                    .success(false)
                    .message("Erreur lors de la synchronisation de l'annulation: " + e.getMessage())
                    .build();
        }
    }

    /**
     * Traitement d'un remboursement hors ligne
     */
    private SyncResultDto traiterRemboursement(SyncOperationDto operation) {
        try {
            JsonNode payload = objectMapper.readTree(operation.getPayload());
            Long reservationId = payload.get("reservationId").asLong();
            String motif = payload.has("motif") ? payload.get("motif").asText() : "Remboursement hors ligne";

            Reservation reservation = reservationService.rembourserReservationSync(reservationId, motif);

            return SyncResultDto.builder()
                    .operationId(operation.getId())
                    .success(true)
                    .message("Remboursement synchronisé avec succès. Réservation: " + reservation.getReservationCode())
                    .conflict(false)
                    .build();

        } catch (Exception e) {
            log.error("Erreur lors du traitement du remboursement: {}", e.getMessage());
            return SyncResultDto.builder()
                    .operationId(operation.getId())
                    .success(false)
                    .message("Erreur lors de la synchronisation du remboursement: " + e.getMessage())
                    .build();
        }
    }
}