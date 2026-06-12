package com.transport.api.incident.entity;

import com.transport.api.common.BaseEntity;
import com.transport.api.incident.enums.TypeIncident;
import com.transport.api.incident.enums.StatutIncident;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "incident")
@Getter
@Setter
public class Incident extends BaseEntity {

    @Column(name = "trajet_id", nullable = false)
    private Long trajetId;

    @Column(name = "reported_by", nullable = false)
    private Long reportedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 50)
    private TypeIncident type;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private StatutIncident status = StatutIncident.OPEN;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;
}