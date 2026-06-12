package com.transport.api.trajet.controller;

import com.transport.api.trajet.dto.LigneCreateRequest;
import com.transport.api.trajet.dto.LigneDto;
import com.transport.api.trajet.service.LigneService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lignes")
@RequiredArgsConstructor
public class LigneController {

    private final LigneService ligneService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LigneDto> createLigne(@Valid @RequestBody LigneCreateRequest request) {
        return new ResponseEntity<>(ligneService.createLigne(request), HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT', 'CLIENT')")
    public ResponseEntity<List<LigneDto>> getAllLignes() {
        return ResponseEntity.ok(ligneService.getAllLignes());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT', 'CLIENT')")
    public ResponseEntity<LigneDto> getLigneById(@PathVariable Long id) {
        return ResponseEntity.ok(ligneService.getLigneById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LigneDto> updateLigne(@PathVariable Long id, @Valid @RequestBody LigneCreateRequest request) {
        return ResponseEntity.ok(ligneService.updateLigne(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteLigne(@PathVariable Long id) {
        ligneService.deleteLigne(id);
        return ResponseEntity.noContent().build();
    }
}