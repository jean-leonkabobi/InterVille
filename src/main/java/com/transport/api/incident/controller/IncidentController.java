package com.transport.api.incident.controller;

import com.transport.api.incident.dto.IncidentDto;
import com.transport.api.incident.enums.StatutIncident;
import com.transport.api.incident.service.IncidentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/incidents")
@RequiredArgsConstructor
public class IncidentController {

    private final IncidentService incidentService;

    /**
     * Admin - Voir tous les incidents
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
    public ResponseEntity<List<IncidentDto>> getAllIncidents() {
        return ResponseEntity.ok(incidentService.getAllIncidents());
    }

    /**
     * Admin - Voir les incidents par statut
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
    public ResponseEntity<List<IncidentDto>> getIncidentsByStatus(@PathVariable StatutIncident status) {
        return ResponseEntity.ok(incidentService.getIncidentsByStatus(status));
    }

    /**
     * Admin - Voir les incidents d'un trajet spécifique
     */
    @GetMapping("/trajet/{trajetId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
    public ResponseEntity<List<IncidentDto>> getIncidentsByTrajet(@PathVariable Long trajetId) {
        return ResponseEntity.ok(incidentService.getIncidentsByTrajet(trajetId));
    }

    /**
     * Admin - Mettre à jour le statut d'un incident
     */
    @PutMapping("/{incidentId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> updateIncidentStatus(
            @PathVariable Long incidentId,
            @RequestParam StatutIncident status) {
        return ResponseEntity.ok(incidentService.updateIncidentStatus(incidentId, status));
    }

    /**
     * Admin - Détails d'un incident
     */
    @GetMapping("/{incidentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
    public ResponseEntity<IncidentDto> getIncident(@PathVariable Long incidentId) {
        return ResponseEntity.ok(incidentService.getIncident(incidentId));
    }
}