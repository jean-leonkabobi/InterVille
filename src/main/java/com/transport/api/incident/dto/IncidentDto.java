package com.transport.api.incident.dto;

import com.transport.api.incident.enums.StatutIncident;
import com.transport.api.incident.enums.TypeIncident;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class IncidentDto {
    private Long id;
    private Long trajetId;
    private String trajetInfo;
    private String reportedBy;
    private TypeIncident type;
    private String description;
    private StatutIncident status;
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;
}