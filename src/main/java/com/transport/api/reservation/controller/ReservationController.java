package com.transport.api.reservation.controller;

import com.transport.api.reservation.dto.ReservationRequest;
import com.transport.api.reservation.dto.ReservationResponse;
import com.transport.api.reservation.service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT', 'CLIENT')")
    public ResponseEntity<ReservationResponse> createReservation(@Valid @RequestBody ReservationRequest request) {
        return new ResponseEntity<>(reservationService.createReservation(request), HttpStatus.CREATED);
    }

    @GetMapping("/{reservationCode}")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT', 'CLIENT')")
    public ResponseEntity<ReservationResponse> getReservation(@PathVariable UUID reservationCode) {
        return ResponseEntity.ok(reservationService.getReservationByCode(reservationCode));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT', 'CLIENT')")
    public ResponseEntity<Void> cancelReservation(@PathVariable Long id) {
        reservationService.cancelReservation(id);
        return ResponseEntity.noContent().build();
    }
}