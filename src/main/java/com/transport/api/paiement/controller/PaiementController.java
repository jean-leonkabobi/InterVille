package com.transport.api.paiement.controller;

import com.transport.api.paiement.dto.PaiementRequest;
import com.transport.api.paiement.dto.PaiementResponse;
import com.transport.api.paiement.service.PaiementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/paiements")
@RequiredArgsConstructor
public class PaiementController {

    private final PaiementService paiementService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT', 'CLIENT')")
    public ResponseEntity<PaiementResponse> processPayment(@Valid @RequestBody PaiementRequest request) {
        return ResponseEntity.ok(paiementService.processPayment(request));
    }
}