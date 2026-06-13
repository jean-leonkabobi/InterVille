package com.transport.api.reservation.controller;

import com.transport.api.reservation.dto.*;
import com.transport.api.reservation.service.VerrouillageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/trajets/{trajetId}/sieges")
@RequiredArgsConstructor
public class SiegeController {

    private final VerrouillageService verrouillageService;

    @GetMapping("/disponibles")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT', 'CLIENT')")
    public ResponseEntity<SiegesDisponiblesResponse> getSiegesDisponibles(@PathVariable Long trajetId) {
        return ResponseEntity.ok(verrouillageService.getSiegesDisponibles(trajetId));
    }

    @GetMapping("/disponibles/public")
    public ResponseEntity<SiegesDisponiblesResponse> getSiegesDisponiblesPublic(@PathVariable Long trajetId) {
        return ResponseEntity.ok(verrouillageService.getSiegesDisponibles(trajetId));
    }

    @PostMapping("/{numeroSiege}/verrouiller")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT', 'CLIENT')")
    public ResponseEntity<VerrouillerResponse> verrouillerSiege(
            @PathVariable Long trajetId,
            @PathVariable String numeroSiege,
            @Valid @RequestBody VerrouillerSiegeRequest request) {
        return ResponseEntity.ok(verrouillageService.verrouillerSiege(trajetId, numeroSiege, request.getSessionId()));
    }

    @PostMapping("/verrouiller-groupes")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT', 'CLIENT')")
    public ResponseEntity<VerrouillerResponse> verrouillerGroupesSieges(
            @PathVariable Long trajetId,
            @Valid @RequestBody VerrouillerGroupesRequest request) {
        return ResponseEntity.ok(verrouillageService.verrouillerGroupesSieges(trajetId, request.getSieges(), request.getSessionId()));
    }

    @DeleteMapping("/{numeroSiege}/liberer")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT', 'CLIENT')")
    public ResponseEntity<Void> libererVerrou(
            @PathVariable Long trajetId,
            @PathVariable String numeroSiege,
            @RequestParam UUID sessionId) {
        verrouillageService.libererVerrou(trajetId, numeroSiege, sessionId);
        return ResponseEntity.noContent().build();
    }
}