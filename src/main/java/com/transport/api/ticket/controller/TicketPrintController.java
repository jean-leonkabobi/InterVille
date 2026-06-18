package com.transport.api.ticket.controller;

import com.transport.api.ticket.dto.TicketPrintDto;
import com.transport.api.ticket.service.TicketPrintService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tickets/print")
@RequiredArgsConstructor
public class TicketPrintController {

    private final TicketPrintService ticketPrintService;

    /**
     * FA7 - Récupérer les données du ticket pour impression
     */
    @GetMapping("/{reservationId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
    public ResponseEntity<TicketPrintDto> getTicketPrintData(@PathVariable Long reservationId) {
        return ResponseEntity.ok(ticketPrintService.getTicketPrintData(reservationId));
    }

    /**
     * FA7 - Récupérer le texte formaté pour impression (80mm)
     */
    @GetMapping(value = "/{reservationId}/text", produces = MediaType.TEXT_PLAIN_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
    public ResponseEntity<String> getTicketPrintText(@PathVariable Long reservationId) {
        TicketPrintDto data = ticketPrintService.getTicketPrintData(reservationId);
        return ResponseEntity.ok(ticketPrintService.generateTicketText(data));
    }

    /**
     * FA7 - Récupérer le QR Code seul (image base64)
     */
    @GetMapping("/{reservationId}/qrcode")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
    public ResponseEntity<String> getTicketQRCode(@PathVariable Long reservationId) {
        TicketPrintDto data = ticketPrintService.getTicketPrintData(reservationId);
        return ResponseEntity.ok(data.getQrCodeBase64());
    }
}