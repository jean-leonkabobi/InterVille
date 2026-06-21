package com.transport.api.admin.controller;

import com.transport.api.admin.dto.TableauBordGlobalDto;
import com.transport.api.admin.service.TableauBordGlobalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final TableauBordGlobalService tableauBordGlobalService;

    /**
     * FG2 - Tableau de bord global
     */
    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TableauBordGlobalDto> getDashboard() {
        return ResponseEntity.ok(tableauBordGlobalService.getTableauBordGlobal());
    }
}