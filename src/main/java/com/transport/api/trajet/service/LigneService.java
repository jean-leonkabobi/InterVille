package com.transport.api.trajet.service;

import com.transport.api.context.TenantContext;
import com.transport.api.common.exception.ResourceNotFoundException;
import com.transport.api.trajet.dto.LigneCreateRequest;
import com.transport.api.trajet.dto.LigneDto;
import com.transport.api.trajet.entity.Ligne;
import com.transport.api.trajet.repository.LigneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LigneService {

    private final LigneRepository ligneRepository;

    @Transactional
    public LigneDto createLigne(LigneCreateRequest request) {
        Ligne ligne = new Ligne();
        ligne.setDepartureCity(request.getDepartureCity());
        ligne.setArrivalCity(request.getArrivalCity());

        // Calculer la durée totale en secondes (cast explicite en long pour éviter l'overflow)
        long totalSeconds = (long) request.getDurationDays() * 24 * 3600 +
                (long) request.getDurationHours() * 3600 +
                (long) request.getDurationMinutes() * 60;

        ligne.setDurationSeconds(totalSeconds);
        ligne.setIsActive(true);
        ligne.setCompanyId(TenantContext.getCurrentTenant());

        Ligne saved = ligneRepository.save(ligne);
        return mapToDto(saved);
    }

    public List<LigneDto> getAllLignes() {
        Long companyId = TenantContext.getCurrentTenant();
        return ligneRepository.findByCompanyId(companyId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public LigneDto getLigneById(Long id) {
        Ligne ligne = findLigneById(id);
        return mapToDto(ligne);
    }

    @Transactional
    public LigneDto updateLigne(Long id, LigneCreateRequest request) {
        Ligne ligne = findLigneById(id);
        ligne.setDepartureCity(request.getDepartureCity());
        ligne.setArrivalCity(request.getArrivalCity());

        // Calculer la durée totale en secondes (cast explicite en long)
        long totalSeconds = (long) request.getDurationDays() * 24 * 3600 +
                (long) request.getDurationHours() * 3600 +
                (long) request.getDurationMinutes() * 60;

        ligne.setDurationSeconds(totalSeconds);

        Ligne updated = ligneRepository.save(ligne);
        return mapToDto(updated);
    }

    @Transactional
    public void deleteLigne(Long id) {
        Ligne ligne = findLigneById(id);
        ligneRepository.delete(ligne);
    }

    private Ligne findLigneById(Long id) {
        Long companyId = TenantContext.getCurrentTenant();
        return ligneRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Ligne non trouvée avec l'id: " + id));
    }

    private LigneDto mapToDto(Ligne ligne) {
        // Formater la durée pour l'affichage
        String durationFormatted = formatDuration(ligne.getDurationSeconds());

        return LigneDto.builder()
                .id(ligne.getId())
                .departureCity(ligne.getDepartureCity())
                .arrivalCity(ligne.getArrivalCity())
                .durationSeconds(ligne.getDurationSeconds())
                .durationFormatted(durationFormatted)
                .isActive(ligne.getIsActive())
                .companyId(ligne.getCompanyId())
                .build();
    }

    private String formatDuration(Long seconds) {
        if (seconds == null) return "0 minute";

        Duration duration = Duration.ofSeconds(seconds);
        long days = duration.toDays();
        long hours = duration.toHoursPart();
        long minutes = duration.toMinutesPart();

        StringBuilder result = new StringBuilder();

        if (days > 0) {
            result.append(days).append(" jour").append(days > 1 ? "s" : "");
            if (hours > 0 || minutes > 0) result.append(" ");
        }

        if (hours > 0) {
            result.append(hours).append(" heure").append(hours > 1 ? "s" : "");
            if (minutes > 0) result.append(" ");
        }

        if (minutes > 0) {
            result.append(minutes).append(" minute").append(minutes > 1 ? "s" : "");
        }

        if (result.length() == 0) {
            return "0 minute";
        }

        return result.toString();
    }
}