package com.transport.api.chauffeur.dto;

import com.transport.api.trajet.enums.StatutTrajet;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StatutTrajetRequest {
    @NotNull(message = "Le statut est requis")
    private StatutTrajet statut;
}