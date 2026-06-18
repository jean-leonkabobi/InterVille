package com.transport.api.reservation.controller;

import com.transport.api.reservation.dto.ModificationReservationRequest;
import com.transport.api.reservation.dto.ReservationAgentRequest;
import com.transport.api.reservation.dto.ReservationResponse;
import com.transport.api.reservation.service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reservations/agent")
@RequiredArgsConstructor
public class ReservationAgentController {

    private final ReservationService reservationService;

    /**
     * FA4 - Réservation pour un client (sans compte)
     */
    @PostMapping
    @PreAuthorize("hasRole('AGENT')")
    public ResponseEntity<ReservationResponse> createReservationByAgent(
            @Valid @RequestBody ReservationAgentRequest request) {
        return new ResponseEntity<>(
                reservationService.createReservationByAgent(request),
                HttpStatus.CREATED
        );
    }

    /**
     * FA10 - Consulter une réservation
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('AGENT')")
    public ResponseEntity<ReservationResponse> getReservationForAgent(@PathVariable Long id) {
        return ResponseEntity.ok(reservationService.getReservationForAgent(id));
    }

    /**
     * FA10 - Modifier une réservation
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('AGENT')")
    public ResponseEntity<ReservationResponse> modifierReservation(
            @PathVariable Long id,
            @Valid @RequestBody ModificationReservationRequest request) {
        return ResponseEntity.ok(reservationService.modifierReservation(id, request));
    }

    /**
     * FA10 - Annuler une réservation (en attente)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('AGENT')")
    public ResponseEntity<String> annulerReservation(
            @PathVariable Long id,
            @RequestBody(required = false) String motif) {
        return ResponseEntity.ok(reservationService.annulerReservation(id, motif));
    }
}