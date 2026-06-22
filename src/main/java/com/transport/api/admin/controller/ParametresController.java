package com.transport.api.admin.controller;

import com.transport.api.admin.dto.ParametresGenerauxDto;
import com.transport.api.admin.service.ParametresService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/parametres")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ParametresController {

    private final ParametresService parametresService;

    @GetMapping
    public ResponseEntity<ParametresGenerauxDto> getParametres() {
        return ResponseEntity.ok(parametresService.getParametres());
    }

    @PutMapping
    public ResponseEntity<ParametresGenerauxDto> updateParametres(@RequestBody ParametresGenerauxDto request) {
        return ResponseEntity.ok(parametresService.updateParametres(request));
    }
}