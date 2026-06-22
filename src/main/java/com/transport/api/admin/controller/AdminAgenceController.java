package com.transport.api.admin.controller;

import com.transport.api.admin.dto.AgenceAdminDto;
import com.transport.api.admin.dto.CreationAgenceRequest;
import com.transport.api.admin.dto.ModificationAgenceRequest;
import com.transport.api.admin.service.GestionAgencesService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/agences")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminAgenceController {

    private final GestionAgencesService gestionAgencesService;

    @GetMapping
    public ResponseEntity<List<AgenceAdminDto>> getAllAgences() {
        return ResponseEntity.ok(gestionAgencesService.getAllAgences());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AgenceAdminDto> getAgenceById(@PathVariable Long id) {
        return ResponseEntity.ok(gestionAgencesService.getAgenceById(id));
    }

    @PostMapping
    public ResponseEntity<AgenceAdminDto> createAgence(@Valid @RequestBody CreationAgenceRequest request) {
        return new ResponseEntity<>(gestionAgencesService.createAgence(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AgenceAdminDto> updateAgence(
            @PathVariable Long id,
            @Valid @RequestBody ModificationAgenceRequest request) {
        return ResponseEntity.ok(gestionAgencesService.updateAgence(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteAgence(@PathVariable Long id) {
        return ResponseEntity.ok(gestionAgencesService.deleteAgence(id));
    }
}