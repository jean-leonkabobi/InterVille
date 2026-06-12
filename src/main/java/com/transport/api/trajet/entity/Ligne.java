package com.transport.api.trajet.entity;

import com.transport.api.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "ligne")
@Getter
@Setter
public class Ligne extends BaseEntity {

    @Column(name = "departure_city", nullable = false, length = 100)
    private String departureCity;

    @Column(name = "arrival_city", nullable = false, length = 100)
    private String arrivalCity;

    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;

    @Column(name = "is_active")
    private Boolean isActive = true;
}