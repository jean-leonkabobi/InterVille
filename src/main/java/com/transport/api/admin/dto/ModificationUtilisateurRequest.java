package com.transport.api.admin.dto;

import com.transport.api.user.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ModificationUtilisateurRequest {

    @NotBlank(message = "Le nom complet est requis")
    private String fullName;

    @NotBlank(message = "Le téléphone est requis")
    private String phone;

    @NotNull(message = "Le rôle est requis")
    private Role role;

    private Long agenceId;

    @NotNull(message = "Le statut actif est requis")
    private Boolean isActive;
}