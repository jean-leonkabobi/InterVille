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

    @NotNull(message = "La durée est requise")
    @Min(value = 1, message = "La durée doit être au moins 1 minute")
    private Integer durationMinutes;
}