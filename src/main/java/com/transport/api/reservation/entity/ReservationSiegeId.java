package com.transport.api.reservation.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservationSiegeId implements Serializable {
    private Long reservationId;
    private Long siegeId;
}