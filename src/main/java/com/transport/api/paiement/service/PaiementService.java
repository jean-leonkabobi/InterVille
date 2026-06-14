package com.transport.api.paiement.service;

import com.transport.api.common.exception.ResourceNotFoundException;
import com.transport.api.paiement.dto.PaiementRequest;
import com.transport.api.paiement.dto.PaiementResponse;
import com.transport.api.paiement.entity.Transaction;
import com.transport.api.paiement.enums.StatutTransaction;
import com.transport.api.paiement.gateway.PaymentGateway;
import com.transport.api.paiement.repository.TransactionRepository;
import com.transport.api.reservation.entity.Reservation;
import com.transport.api.reservation.enums.StatutReservation;
import com.transport.api.reservation.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaiementService {

    private final List<PaymentGateway> gateways;
    private final TransactionRepository transactionRepository;
    private final ReservationRepository reservationRepository;
    private final ConversionService conversionService;

    @Transactional
    public PaiementResponse processPayment(PaiementRequest request) {
        // 1. Vérifier que la réservation existe
        Reservation reservation = reservationRepository.findById(request.getReservationId())
                .orElseThrow(() -> new ResourceNotFoundException("Réservation non trouvée"));

        if (reservation.getStatus() != StatutReservation.PENDING) {
            throw new RuntimeException("La réservation n'est pas en attente de paiement");
        }

        // 2. Vérifier que le montant est correct
        double montantAttendu = reservation.getTotalPrice().doubleValue();
        double montantConverti = conversionService.convertirEnCDF(request.getMontant(), request.getDevise());

        if (Math.abs(montantConverti - montantAttendu) > 1) {
            throw new RuntimeException("Montant incorrect. Attendu: " + montantAttendu + " CDF, Reçu: " + montantConverti);
        }

        // 3. Trouver le gateway approprié
        PaymentGateway gateway = gateways.stream()
                .filter(g -> g.supports(request.getModePaiement()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Mode de paiement non supporté"));

        // 4. Traiter le paiement
        PaiementResponse paymentResponse = gateway.processPayment(request);

        // 5. Sauvegarder la transaction
        Transaction transaction = new Transaction();
        transaction.setCompanyId(reservation.getCompanyId());
        transaction.setReservationId(reservation.getId());
        transaction.setAgenceId(request.getAgentId());
        transaction.setAmount(BigDecimal.valueOf(paymentResponse.getMontantConvertiCDF()));
        transaction.setPaymentMode(request.getModePaiement());
        transaction.setMobileMoneyRef(paymentResponse.getTransactionReference());
        transaction.setStatus(paymentResponse.getStatut());
        transaction.setPaymentDate(paymentResponse.getPaymentDate());
        transaction.setCreatedAt(java.time.LocalDateTime.now());
        transaction.setUpdatedAt(java.time.LocalDateTime.now());

        transactionRepository.save(transaction);

        // 6. Mettre à jour la réservation si paiement réussi
        if (paymentResponse.getStatut() == StatutTransaction.SUCCESS) {
            reservation.setStatus(StatutReservation.PAID);
            reservation.setUpdatedAt(java.time.LocalDateTime.now());
            reservationRepository.save(reservation);
        }

        return paymentResponse;
    }
}