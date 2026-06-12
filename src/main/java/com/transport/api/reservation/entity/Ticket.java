package com.transport.api.reservation.entity;

import com.transport.api.reservation.enums.StatutTicket;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "ticket")
@Getter
@Setter
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reservation_id", nullable = false)
    private Long reservationId;

    @Column(name = "qr_code", nullable = false, unique = true, length = 255)
    private String qrCode;

    @Column(name = "validated_at")
    private LocalDateTime validatedAt;

    @Column(name = "validated_by")
    private Long validatedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private StatutTicket status = StatutTicket.ISSUED;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}