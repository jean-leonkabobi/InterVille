package com.transport.api.admin.dto;

import com.transport.api.user.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreationUtilisateurRequest {

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

    @NotNull(message = "Le rôle est requis")
    private Role role;

    private Long agenceId; // Obligatoire pour AGENT
}