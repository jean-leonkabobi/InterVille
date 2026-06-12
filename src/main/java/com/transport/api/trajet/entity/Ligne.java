package com.transport.api.trajet.entity;

import com.transport.api.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Duration;

@Entity
@Table(name = "ligne")
@Getter
@Setter
public class Ligne extends BaseEntity {

    @Column(name = "departure_city", nullable = false, length = 100)
    private String departureCity;

    @Column(name = "arrival_city", nullable = false, length = 100)
    private String arrivalCity;

    @Column(name = "duration_seconds", nullable = false)
    private Long durationSeconds; // Stocké en secondes en base

    @Column(name = "is_active")
    private Boolean isActive = true;

    // Méthode utilitaire pour obtenir la durée en Duration
    public Duration getDuration() {
        return Duration.ofSeconds(durationSeconds);
    }

    // Méthode utilitaire pour définir la durée
    public void setDuration(Duration duration) {
        this.durationSeconds = duration.getSeconds();
    }
}