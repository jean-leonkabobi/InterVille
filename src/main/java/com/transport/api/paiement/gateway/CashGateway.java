package com.transport.api.paiement.gateway;

import com.transport.api.paiement.dto.PaiementRequest;
import com.transport.api.paiement.dto.PaiementResponse;
import com.transport.api.paiement.enums.ModePaiement;
import com.transport.api.paiement.enums.StatutTransaction;
import com.transport.api.paiement.service.ConversionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CashGateway implements PaymentGateway {

    private final ConversionService conversionService;

    @Override
    public PaiementResponse processPayment(PaiementRequest request) {
        // Paiement en espèces - validation immédiate
        String transactionRef = "CSH" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        double montantCDF = conversionService.convertirEnCDF(request.getMontant(), request.getDevise());

        return PaiementResponse.builder()
                .id(1L)
                .reservationId(request.getReservationId())
                .modePaiement(ModePaiement.CASH)
                .montant(request.getMontant())
                .devise(request.getDevise())
                .montantConvertiCDF(montantCDF)
                .statut(StatutTransaction.SUCCESS)
                .transactionReference(transactionRef)
                .paymentDate(LocalDateTime.now())
                .build();
    }

    @Override
    public boolean supports(ModePaiement mode) {
        return mode == ModePaiement.CASH;
    }
}