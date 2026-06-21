package com.transport.api.chauffeur.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ValidationManuelleRequest {
    @NotNull(message = "L'ID du trajet est requis")
    private Long trajetId;

    @NotBlank(message = "Le nom du passager est requis")
    private String passengerName;
}