package com.transport.api.reservation.controller;

import com.transport.api.reservation.dto.ClientTicketDto;
import com.transport.api.reservation.dto.TicketDetailDto;
import com.transport.api.reservation.service.ClientTicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
public class ClientTicketController {

    private final ClientTicketService clientTicketService;

    /**
     * FC10 - Historique des billets du client connecté
     */
    @GetMapping("/mes-billets")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<List<ClientTicketDto>> getMesTickets() {
        return ResponseEntity.ok(clientTicketService.getMesTickets());
    }

    /**
     * FC10 - Historique avec filtre (optionnel)
     */
    @GetMapping("/mes-billets/filter")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<List<ClientTicketDto>> getMesTicketsByStatus(@RequestParam String status) {
        return ResponseEntity.ok(clientTicketService.getMesTicketsByStatus(status));
    }

    /**
     * FC11 - Détails d'un billet spécifique
     */
    @GetMapping("/billets/{reservationId}")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<TicketDetailDto> getTicketDetail(@PathVariable Long reservationId) {
        return ResponseEntity.ok(clientTicketService.getTicketDetail(reservationId));
    }
}