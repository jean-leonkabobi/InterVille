package com.transport.api.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ModificationProfilRequest {

    @NotBlank(message = "Le nom complet est requis")
    private String fullName;

    @NotBlank(message = "Le téléphone est requis")
    private String phone;

    @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caractères")
    private String newPassword;  // Optionnel
}