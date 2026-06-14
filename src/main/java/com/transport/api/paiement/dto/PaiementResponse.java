package com.transport.api.paiement.dto;

import com.transport.api.paiement.enums.Devise;
import com.transport.api.paiement.enums.ModePaiement;
import com.transport.api.paiement.enums.StatutTransaction;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class PaiementResponse {
    private Long id;
    private Long reservationId;
    private ModePaiement modePaiement;
    private Double montant;
    private Devise devise;
    private Double montantConvertiCDF;
    private StatutTransaction statut;
    private String transactionReference;
    private LocalDateTime paymentDate;
}