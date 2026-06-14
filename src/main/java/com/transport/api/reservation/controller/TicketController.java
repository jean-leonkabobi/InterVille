package com.transport.api.reservation.controller;

import com.transport.api.reservation.dto.TicketDto;
import com.transport.api.reservation.dto.TicketValidationRequest;
import com.transport.api.reservation.dto.TicketValidationResponse;
import com.transport.api.reservation.service.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    @PostMapping("/generate/{reservationId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT', 'CLIENT')")
    public ResponseEntity<TicketDto> generateTicket(@PathVariable Long reservationId) {
        return ResponseEntity.ok(ticketService.generateTicket(reservationId));
    }

    @GetMapping("/reservation/{reservationId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT', 'CLIENT')")
    public ResponseEntity<TicketDto> getTicketByReservation(@PathVariable Long reservationId) {
        return ResponseEntity.ok(ticketService.getTicketByReservationId(reservationId));
    }

    @GetMapping("/{qrCode}")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT', 'CLIENT', 'CHAUFFEUR')")
    public ResponseEntity<TicketDto> getTicketByQrCode(@PathVariable String qrCode) {
        return ResponseEntity.ok(ticketService.getTicketByQrCode(qrCode));
    }

    @PostMapping("/validate")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT', 'CHAUFFEUR')")
    public ResponseEntity<TicketValidationResponse> validateTicket(@Valid @RequestBody TicketValidationRequest request) {
        return ResponseEntity.ok(ticketService.validateTicket(request));
    }
}