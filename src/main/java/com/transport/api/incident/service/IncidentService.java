package com.transport.api.incident.service;

import com.transport.api.incident.dto.IncidentDto;
import com.transport.api.incident.entity.Incident;
import com.transport.api.incident.enums.StatutIncident;
import com.transport.api.incident.repository.IncidentRepository;
import com.transport.api.trajet.entity.Trajet;
import com.transport.api.trajet.repository.TrajetRepository;
import com.transport.api.user.entity.User;
import com.transport.api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class IncidentService {

    private final IncidentRepository incidentRepository;
    private final TrajetRepository trajetRepository;
    private final UserRepository userRepository;

    public List<IncidentDto> getAllIncidents() {
        return incidentRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public List<IncidentDto> getIncidentsByStatus(StatutIncident status) {
        return incidentRepository.findByStatus(status).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public List<IncidentDto> getIncidentsByTrajet(Long trajetId) {
        return incidentRepository.findByTrajetId(trajetId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public IncidentDto getIncident(Long incidentId) {
        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new RuntimeException("Incident non trouvé"));
        return mapToDto(incident);
    }

    public String updateIncidentStatus(Long incidentId, StatutIncident status) {
        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new RuntimeException("Incident non trouvé"));

        incident.setStatus(status);
        if (status == StatutIncident.RESOLVED || status == StatutIncident.CLOSED) {
            incident.setResolvedAt(LocalDateTime.now());
        }
        incidentRepository.save(incident);

        return "Statut de l'incident mis à jour avec succès: " + status.name();
    }

    private IncidentDto mapToDto(Incident incident) {
        Trajet trajet = trajetRepository.findById(incident.getTrajetId()).orElse(null);
        User chauffeur = userRepository.findById(incident.getReportedBy()).orElse(null);

        return IncidentDto.builder()
                .id(incident.getId())
                .trajetId(incident.getTrajetId())
                .trajetInfo(trajet != null ? trajet.getDepartureTime().toString() : "N/A")
                .reportedBy(chauffeur != null ? chauffeur.getFullName() : "Inconnu")
                .type(incident.getType())
                .description(incident.getDescription())
                .status(incident.getStatus())
                .createdAt(incident.getCreatedAt())
                .resolvedAt(incident.getResolvedAt())
                .build();
    }
}