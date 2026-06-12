package com.transport.api.reservation.entity;

import com.transport.api.common.BaseEntity;
import com.transport.api.reservation.enums.StatutReservation;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "reservation")
@Getter
@Setter
public class Reservation extends BaseEntity {

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "trajet_id", nullable = false)
    private Long trajetId;

    @Column(name = "agence_id")
    private Long agenceId;

    @Column(name = "reservation_code", unique = true)
    private UUID reservationCode = UUID.randomUUID();

    @Column(name = "passenger_name", length = 100)
    private String passengerName;

    @Column(name = "passenger_phone", length = 20)
    private String passengerPhone;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private StatutReservation status = StatutReservation.PENDING;

    @Column(name = "total_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;
}