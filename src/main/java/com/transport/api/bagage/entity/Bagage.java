package com.transport.api.bagage.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "bagage")
@Getter
@Setter
public class Bagage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reservation_id", nullable = false)
    private Long reservationId;

    @Column(name = "passenger_index")
    private Integer passengerIndex = 1;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "label_printed")
    private Boolean labelPrinted = false;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}