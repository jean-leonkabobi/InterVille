package com.transport.api.trajet.entity;

import com.transport.api.common.BaseEntity;
import com.transport.api.trajet.enums.StatutTrajet;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "trajet")
@Getter
@Setter
public class Trajet extends BaseEntity {

    @Column(name = "ligne_id", nullable = false)
    private Long ligneId;

    @Column(name = "bus_id", nullable = false)
    private Long busId;

    @Column(name = "chauffeur_id")
    private Long chauffeurId;

    @Column(name = "departure_time", nullable = false)
    private LocalDateTime departureTime;

    @Column(name = "arrival_time", nullable = false)
    private LocalDateTime arrivalTime;

    @Column(name = "base_price", nullable = false)
    private Double basePrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private StatutTrajet status = StatutTrajet.SCHEDULED;
}