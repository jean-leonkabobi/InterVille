package com.transport.api.reservation.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "reservation_siege")
@IdClass(ReservationSiegeId.class)
@Getter
@Setter
public class ReservationSiege {

    @Id
    @Column(name = "reservation_id", nullable = false)
    private Long reservationId;

    @Id
    @Column(name = "siege_id", nullable = false)
    private Long siegeId;

    @Column(name = "trajet_id", nullable = false)
    private Long trajetId;
}