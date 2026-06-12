package com.transport.api.paiement.entity;

import com.transport.api.common.BaseEntity;
import com.transport.api.paiement.enums.ModePaiement;
import com.transport.api.paiement.enums.StatutTransaction;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "transaction")
@Getter
@Setter
public class Transaction extends BaseEntity {

    @Column(name = "reservation_id", nullable = false)
    private Long reservationId;

    @Column(name = "agence_id")
    private Long agenceId;

    @Column(name = "amount", nullable = false)
    private Double amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_mode", nullable = false, length = 20)
    private ModePaiement paymentMode;

    @Column(name = "mobile_money_ref", length = 100)
    private String mobileMoneyRef;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private StatutTransaction status = StatutTransaction.PENDING;

    @Column(name = "payment_date")
    private LocalDateTime paymentDate;
}