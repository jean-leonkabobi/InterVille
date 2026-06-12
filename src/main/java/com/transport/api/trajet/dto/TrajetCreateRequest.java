package com.transport.api.trajet.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TrajetCreateRequest {

    @NotNull(message = "L'ID de la ligne est requis")
    private Long ligneId;

    @NotNull(message = "L'ID du bus est requis")
    private Long busId;

    private Long chauffeurId;

    @NotNull(message = "L'heure de départ est requise")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime departureTime;

    @NotNull(message = "L'heure d'arrivée est requise")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime arrivalTime;

    @NotNull(message = "Le prix est requis")
    @Min(value = 0, message = "Le prix doit être positif")
    private Double basePrice;
}