package com.transport.api.trajet.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class RechercheTrajetRequest {

    @NotBlank(message = "La ville de départ est requise")
    private String departureCity;

    @NotBlank(message = "La ville d'arrivée est requise")
    private String arrivalCity;

    @NotNull(message = "La date est requise")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;

    private Integer passengerCount = 1;
}