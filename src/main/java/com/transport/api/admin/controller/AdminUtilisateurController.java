package com.transport.api.admin.controller;

import com.transport.api.admin.dto.CreationUtilisateurRequest;
import com.transport.api.admin.dto.ModificationUtilisateurRequest;
import com.transport.api.admin.dto.UtilisateurAdminDto;
import com.transport.api.admin.service.GestionUtilisateursService;
import com.transport.api.user.enums.Role;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminUtilisateurController {

    private final GestionUtilisateursService gestionUtilisateursService;

    /**
     * FG3 - Liste des utilisateurs
     */
    @GetMapping
    public ResponseEntity<List<UtilisateurAdminDto>> getAllUsers() {
        return ResponseEntity.ok(gestionUtilisateursService.getAllUsers());
    }

    /**
     * FG3 - Rechercher un utilisateur par ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<UtilisateurAdminDto> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(gestionUtilisateursService.getUserById(id));
    }

    /**
     * FG3 - Rechercher des utilisateurs par rôle
     */
    @GetMapping("/role/{role}")
    public ResponseEntity<List<UtilisateurAdminDto>> getUsersByRole(@PathVariable Role role) {
        return ResponseEntity.ok(gestionUtilisateursService.getUsersByRole(role));
    }

    /**
     * FG3 - Création d'un utilisateur
     */
    @PostMapping
    public ResponseEntity<UtilisateurAdminDto> createUser(@Valid @RequestBody CreationUtilisateurRequest request) {
        return new ResponseEntity<>(gestionUtilisateursService.createUser(request), HttpStatus.CREATED);
    }

    /**
     * FG3 - Modification d'un utilisateur
     */
    @PutMapping("/{id}")
    public ResponseEntity<UtilisateurAdminDto> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody ModificationUtilisateurRequest request) {
        return ResponseEntity.ok(gestionUtilisateursService.updateUser(id, request));
    }

    /**
     * FG3 - Suspension/Activation d'un utilisateur
     */
    @PatchMapping("/{id}/toggle")
    public ResponseEntity<String> toggleUserStatus(@PathVariable Long id) {
        return ResponseEntity.ok(gestionUtilisateursService.toggleUserStatus(id));
    }

    /**
     * FG3 - Suppression d'un utilisateur
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        return ResponseEntity.ok(gestionUtilisateursService.deleteUser(id));
    }
}