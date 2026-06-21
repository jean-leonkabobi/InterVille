package com.transport.api.reservation.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AnnulationClientRequest {
    @NotBlank(message = "Le motif d'annulation est requis")
    private String motif;
}