package com.transport.api.paiement.gateway;

import com.transport.api.paiement.dto.PaiementRequest;
import com.transport.api.paiement.dto.PaiementResponse;
import com.transport.api.paiement.enums.ModePaiement;
import com.transport.api.paiement.enums.StatutTransaction;
import com.transport.api.paiement.service.ConversionService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class MpesaGateway implements PaymentGateway {

    private static final Logger logger = LoggerFactory.getLogger(MpesaGateway.class);
    private final ConversionService conversionService;

    @Override
    public PaiementResponse processPayment(PaiementRequest request) {
        logger.info("Traitement paiement M-Pesa pour réservation {} au {}",
                request.getReservationId(), request.getPhoneNumber());

        // Simulation : toujours succès pour les tests
        // Plus tard : appel réel à l'API M-Pesa ou Pawapay
        String transactionRef = "MP" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        double montantCDF = conversionService.convertirEnCDF(request.getMontant(), request.getDevise());

        return PaiementResponse.builder()
                .id(1L)
                .reservationId(request.getReservationId())
                .modePaiement(ModePaiement.MPESA)
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
        return mode == ModePaiement.MPESA;
    }
}