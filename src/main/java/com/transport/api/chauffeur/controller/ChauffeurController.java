package com.transport.api.chauffeur.controller;

import com.transport.api.chauffeur.dto.ManifesteDto;
import com.transport.api.chauffeur.dto.MissionDto;
import com.transport.api.chauffeur.dto.StatutTrajetRequest;
import com.transport.api.chauffeur.dto.ValidationManuelleRequest;
import com.transport.api.chauffeur.service.ChauffeurService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chauffeur")
@RequiredArgsConstructor
public class ChauffeurController {

    private final ChauffeurService chauffeurService;

    /**
     * FD2 - Missions du jour
     */
    @GetMapping("/missions")
    @PreAuthorize("hasRole('CHAUFFEUR')")
    public ResponseEntity<List<MissionDto>> getMissionsDuJour() {
        return ResponseEntity.ok(chauffeurService.getMissionsDuJour());
    }

    /**
     * FD3 - Manifeste des passagers
     */
    @GetMapping("/missions/{trajetId}/manifeste")
    @PreAuthorize("hasRole('CHAUFFEUR')")
    public ResponseEntity<ManifesteDto> getManifeste(@PathVariable Long trajetId) {
        return ResponseEntity.ok(chauffeurService.getManifeste(trajetId));
    }

    /**
     * FD5 - Validation manuelle d'un passager
     */
    @PostMapping("/validation-manuelle")
    @PreAuthorize("hasRole('CHAUFFEUR')")
    public ResponseEntity<String> validationManuelle(@Valid @RequestBody ValidationManuelleRequest request) {
        return ResponseEntity.ok(chauffeurService.validationManuelle(request));
    }

    /**
     * FD6 - Mise à jour du statut du trajet
     */
    @PutMapping("/missions/{trajetId}/statut")
    @PreAuthorize("hasRole('CHAUFFEUR')")
    public ResponseEntity<String> updateStatutTrajet(
            @PathVariable Long trajetId,
            @Valid @RequestBody StatutTrajetRequest request) {
        return ResponseEntity.ok(chauffeurService.updateStatutTrajet(trajetId, request.getStatut()));
    }

    /**
     * FD7 - Historique des missions (30 jours)
     */
    @GetMapping("/missions/historique")
    @PreAuthorize("hasRole('CHAUFFEUR')")
    public ResponseEntity<List<MissionDto>> getHistoriqueMissions() {
        return ResponseEntity.ok(chauffeurService.getHistoriqueMissions());
    }
}