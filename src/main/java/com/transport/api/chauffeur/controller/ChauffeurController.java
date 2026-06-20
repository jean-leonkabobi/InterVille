package com.transport.api.chauffeur.controller;

import com.transport.api.chauffeur.dto.MissionDto;
import com.transport.api.chauffeur.service.ChauffeurService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}