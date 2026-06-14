package com.transport.api.paiement.dto;

import com.transport.api.paiement.enums.Devise;
import com.transport.api.paiement.enums.ModePaiement;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class PaiementRequest {

    @NotNull(message = "L'ID de réservation est requis")
    private Long reservationId;

    @NotNull(message = "Le mode de paiement est requis")
    private ModePaiement modePaiement;

    @NotNull(message = "La devise est requise")
    private Devise devise;

    @Positive(message = "Le montant doit être positif")
    private Double montant;

    // Pour Mobile Money
    private String phoneNumber;

    // Pour guichet (optionnel)
    private Long agentId;
}