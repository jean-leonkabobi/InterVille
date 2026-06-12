package com.transport.api.reservation.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "verrou_siege", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"trajet_id", "siege_id"})
})
@Getter
@Setter
public class VerrouSiege {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "trajet_id", nullable = false)
    private Long trajetId;

    @Column(name = "siege_id", nullable = false)
    private Long siegeId;

    @Column(name = "reservation_session_id", nullable = false)
    private UUID reservationSessionId;

    @Column(name = "locked_at", nullable = false)
    private LocalDateTime lockedAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
}