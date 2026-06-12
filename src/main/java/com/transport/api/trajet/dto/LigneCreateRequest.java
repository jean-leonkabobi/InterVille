package com.transport.api.trajet.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LigneCreateRequest {

    @NotBlank(message = "La ville de départ est requise")
    private String departureCity;

    @NotBlank(message = "La ville d'arrivée est requise")
    private String arrivalCity;

    @NotNull(message = "Le nombre de jours est requis")
    @Min(value = 0, message = "Le nombre de jours doit être positif")
    private Integer durationDays;

    @NotNull(message = "Le nombre d'heures est requis")
    @Min(value = 0, message = "Le nombre d'heures doit être positif")
    private Integer durationHours;

    @NotNull(message = "Le nombre de minutes est requis")
    @Min(value = 0, message = "Le nombre de minutes doit être positif")
    private Integer durationMinutes;
}