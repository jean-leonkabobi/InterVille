package com.transport.api.bus.entity;

import com.transport.api.common.BaseEntity;
import com.transport.api.bus.enums.StatutBus;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;

@Entity
@Table(name = "bus")
@Getter
@Setter
public class Bus extends BaseEntity {

    @Column(name = "registration", nullable = false, unique = true, length = 50)
    private String registration;

    @Column(name = "capacity", nullable = false)
    private Integer capacity;

    @Type(JsonType.class)
    @Column(name = "seat_config", columnDefinition = "jsonb")
    private String seatConfig; // Stocké en JSON

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private StatutBus status = StatutBus.OPERATIONAL;
}