package com.transport.api.bus.entity;

import com.transport.api.common.BaseEntity;
import com.transport.api.bus.enums.StatutBus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "bus")
@Getter
@Setter
public class Bus extends BaseEntity {

    @Column(name = "registration", nullable = false, unique = true, length = 50)
    private String registration;

    @Column(name = "capacity", nullable = false)
    private Integer capacity;

    @Column(name = "seat_config", columnDefinition = "JSONB")
    private String seatConfig; // Stocké en JSON

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private StatutBus status = StatutBus.OPERATIONAL;
}