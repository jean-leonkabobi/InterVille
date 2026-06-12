package com.transport.api.auth.dto;

import com.transport.api.user.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "L'email est requis")
    @Email(message = "Format email invalide")
    private String email;

    @NotBlank(message = "Le mot de passe est requis")
    @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caractères")
    private String password;

    @NotBlank(message = "Le nom complet est requis")
    private String fullName;

    @NotBlank(message = "Le téléphone est requis")
    private String phone;

    private Role role = Role.CLIENT; // Par défaut

    private Long agenceId; // Pour AGENT uniquement
}