package com.transport.api.user.controller;

import com.transport.api.user.dto.ModificationProfilRequest;
import com.transport.api.user.dto.ProfilClientDto;
import com.transport.api.user.service.ProfilClientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/clients/profil")
@RequiredArgsConstructor
public class ProfilClientController {

    private final ProfilClientService profilClientService;

    /**
     * FC3 - Consultation du profil
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN', 'AGENT')")
    public ResponseEntity<ProfilClientDto> getProfil() {
        return ResponseEntity.ok(profilClientService.getProfil());
    }

    /**
     * FC3 - Modification du profil
     */
    @PutMapping
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN', 'AGENT')")
    public ResponseEntity<ProfilClientDto> modifierProfil(@Valid @RequestBody ModificationProfilRequest request) {
        return ResponseEntity.ok(profilClientService.modifierProfil(request));
    }
}