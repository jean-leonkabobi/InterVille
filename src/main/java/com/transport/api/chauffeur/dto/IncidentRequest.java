package com.transport.api.chauffeur.dto;

import com.transport.api.incident.enums.TypeIncident;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class IncidentRequest {
    @NotNull(message = "L'ID du trajet est requis")
    private Long trajetId;

    @NotNull(message = "Le type d'incident est requis")
    private TypeIncident type;

    @NotBlank(message = "La description est requise")
    private String description;
}