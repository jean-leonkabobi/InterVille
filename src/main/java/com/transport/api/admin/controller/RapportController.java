package com.transport.api.admin.controller;

import com.transport.api.admin.dto.*;
import com.transport.api.admin.service.RapportsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/rapports")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
public class RapportController {

    private final RapportsService rapportsService;

    /**
     * FG11 - Rapport général des ventes
     */
    @PostMapping("/ventes")
    public ResponseEntity<RapportVentesDto> getRapportVentes(@Valid @RequestBody RapportPeriodeRequest request) {
        return ResponseEntity.ok(rapportsService.getRapportVentes(request));
    }

    /**
     * FG11 - Rapport des ventes par agence
     */
    @PostMapping("/ventes/agences")
    public ResponseEntity<List<RapportVentesAgenceDto>> getRapportVentesParAgence(@Valid @RequestBody RapportPeriodeRequest request) {
        return ResponseEntity.ok(rapportsService.getRapportVentesParAgence(request));
    }

    /**
     * FG11 - Rapport des ventes par ligne
     */
    @PostMapping("/ventes/lignes")
    public ResponseEntity<List<RapportVentesLigneDto>> getRapportVentesParLigne(@Valid @RequestBody RapportPeriodeRequest request) {
        return ResponseEntity.ok(rapportsService.getRapportVentesParLigne(request));
    }
}