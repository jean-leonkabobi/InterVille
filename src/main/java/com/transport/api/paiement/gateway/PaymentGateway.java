package com.transport.api.paiement.gateway;

import com.transport.api.paiement.dto.PaiementRequest;
import com.transport.api.paiement.dto.PaiementResponse;
import com.transport.api.paiement.enums.ModePaiement;

public interface PaymentGateway {
    PaiementResponse processPayment(PaiementRequest request);
    boolean supports(ModePaiement mode);
}