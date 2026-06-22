package com.transport.api.sync.controller;

import com.transport.api.sync.dto.SyncRequest;
import com.transport.api.sync.dto.SyncResponse;
import com.transport.api.sync.service.SyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sync")
@RequiredArgsConstructor
public class SyncController {

    private final SyncService syncService;

    /**
     * FA13 - Synchronisation des opérations hors ligne
     */
    @PostMapping
    @PreAuthorize("hasRole('AGENT')")
    public ResponseEntity<SyncResponse> synchroniser(@RequestBody SyncRequest request) {
        return ResponseEntity.ok(syncService.synchroniser(request));
    }
}