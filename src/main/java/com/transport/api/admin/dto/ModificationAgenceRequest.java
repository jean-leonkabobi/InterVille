package com.transport.api.admin.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ModificationAgenceRequest {

    @NotBlank(message = "Le nom de l'agence est requis")
    private String name;

    private String address;

    @NotBlank(message = "Le téléphone est requis")
    private String phone;

    @Email(message = "Format email invalide")
    private String email;

    @NotNull(message = "Le statut actif est requis")
    private Boolean isActive;
}