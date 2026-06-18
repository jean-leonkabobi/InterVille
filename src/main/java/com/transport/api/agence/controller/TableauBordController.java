package com.transport.api.agence.controller;

import com.transport.api.agence.dto.TableauBordAgenceDto;
import com.transport.api.agence.service.TableauBordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/agence/tableau-bord")
@RequiredArgsConstructor
public class TableauBordController {

    private final TableauBordService tableauBordService;

    @GetMapping
    @PreAuthorize("hasRole('AGENT')")
    public ResponseEntity<TableauBordAgenceDto> getTableauBord() {
        // Récupérer l'agence de l'agent connecté via le contexte
        // Pour l'instant, on utilise agenceId = 1
        Long agenceId = 1L;
        return ResponseEntity.ok(tableauBordService.getTableauBord(agenceId));
    }
}