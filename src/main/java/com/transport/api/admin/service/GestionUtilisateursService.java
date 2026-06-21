package com.transport.api.admin.service;

import com.transport.api.admin.dto.CreationUtilisateurRequest;
import com.transport.api.admin.dto.ModificationUtilisateurRequest;
import com.transport.api.admin.dto.UtilisateurAdminDto;
import com.transport.api.agence.entity.Agence;
import com.transport.api.agence.repository.AgenceRepository;
import com.transport.api.common.exception.ResourceNotFoundException;
import com.transport.api.user.entity.User;
import com.transport.api.user.enums.Role;
import com.transport.api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GestionUtilisateursService {

    private final UserRepository userRepository;
    private final AgenceRepository agenceRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * FG3 - Liste des utilisateurs
     */
    public List<UtilisateurAdminDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * FG3 - Rechercher un utilisateur par ID
     */
    public UtilisateurAdminDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));
        return mapToDto(user);
    }

    /**
     * FG3 - Rechercher des utilisateurs par rôle
     */
    public List<UtilisateurAdminDto> getUsersByRole(Role role) {
        return userRepository.findByRole(role).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * FG3 - Création d'un utilisateur (par l'admin)
     */
    @Transactional
    public UtilisateurAdminDto createUser(CreationUtilisateurRequest request) {
        // Vérifier si l'email existe déjà
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email déjà utilisé");
        }

        // Pour un AGENT, vérifier que l'agence existe
        if (request.getRole() == Role.AGENT && request.getAgenceId() == null) {
            throw new RuntimeException("L'agence est requise pour un agent");
        }

        // Vérifier l'agence si fournie
        if (request.getAgenceId() != null) {
            agenceRepository.findById(request.getAgenceId())
                    .orElseThrow(() -> new ResourceNotFoundException("Agence non trouvée"));
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());
        user.setRole(request.getRole());
        user.setAgenceId(request.getAgenceId());
        user.setIsActive(true);
        user.setEmailVerified(true); // Créé par admin, donc déjà vérifié
        user.setCompanyId(1L); // V1: une seule compagnie

        userRepository.save(user);
        return mapToDto(user);
    }

    /**
     * FG3 - Modification d'un utilisateur (par l'admin)
     */
    @Transactional
    public UtilisateurAdminDto updateUser(Long id, ModificationUtilisateurRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        // Si l'utilisateur devient AGENT, vérifier l'agence
        if (request.getRole() == Role.AGENT && request.getAgenceId() == null) {
            throw new RuntimeException("L'agence est requise pour un agent");
        }

        // Vérifier l'agence si fournie
        if (request.getAgenceId() != null) {
            agenceRepository.findById(request.getAgenceId())
                    .orElseThrow(() -> new ResourceNotFoundException("Agence non trouvée"));
        }

        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());
        user.setRole(request.getRole());
        user.setAgenceId(request.getAgenceId());
        user.setIsActive(request.getIsActive());
        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);
        return mapToDto(user);
    }

    /**
     * FG3 - Suspension/Activation d'un utilisateur
     */
    @Transactional
    public String toggleUserStatus(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        user.setIsActive(!user.getIsActive());
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        return "Utilisateur " + (user.getIsActive() ? "activé" : "suspendu") + " avec succès";
    }

    /**
     * FG3 - Suppression d'un utilisateur (soft delete)
     */
    @Transactional
    public String deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        // Marquer comme supprimé (soft delete)
        user.setDeletedAt(LocalDateTime.now());
        user.setIsActive(false);
        userRepository.save(user);

        return "Utilisateur supprimé avec succès";
    }

    private UtilisateurAdminDto mapToDto(User user) {
        String agenceNom = null;

        if (user.getAgenceId() != null) {
            // Récupérer l'agence directement
            Agence agence = agenceRepository.findById(user.getAgenceId()).orElse(null);
            if (agence != null) {
                agenceNom = agence.getName();
            }
        }

        return UtilisateurAdminDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .role(user.getRole())
                .agenceId(user.getAgenceId())
                .agenceNom(agenceNom)
                .isActive(user.getIsActive())
                .emailVerified(user.isEmailVerified())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}