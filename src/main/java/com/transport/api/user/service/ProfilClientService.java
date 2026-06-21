package com.transport.api.user.service;

import com.transport.api.user.dto.ModificationProfilRequest;
import com.transport.api.user.dto.ProfilClientDto;
import com.transport.api.user.entity.User;
import com.transport.api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProfilClientService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * FC3 - Consultation du profil
     */
    public ProfilClientDto getProfil() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        return mapToDto(user);
    }

    /**
     * FC3 - Modification du profil
     */
    @Transactional
    public ProfilClientDto modifierProfil(ModificationProfilRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        // Mettre à jour les champs
        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());

        // Modifier le mot de passe si fourni
        if (request.getNewPassword() != null && !request.getNewPassword().isEmpty()) {
            user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        }

        user.setUpdatedAt(java.time.LocalDateTime.now());
        userRepository.save(user);

        return mapToDto(user);
    }

    private ProfilClientDto mapToDto(User user) {
        return ProfilClientDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .role(user.getRole())
                .emailVerified(user.isEmailVerified())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}