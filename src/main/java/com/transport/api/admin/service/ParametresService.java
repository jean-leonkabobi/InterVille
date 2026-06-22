package com.transport.api.admin.service;

import com.transport.api.admin.dto.ParametresGenerauxDto;
import com.transport.api.admin.entity.ParametresGeneraux;
import com.transport.api.admin.repository.ParametresRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ParametresService {

    private final ParametresRepository parametresRepository;

    /**
     * Récupère les paramètres généraux (un seul enregistrement)
     */
    public ParametresGenerauxDto getParametres() {
        ParametresGeneraux params = getOrCreateParams();
        return mapToDto(params);
    }

    /**
     * Met à jour les paramètres généraux
     */
    @Transactional
    public ParametresGenerauxDto updateParametres(ParametresGenerauxDto request) {
        ParametresGeneraux params = getOrCreateParams();

        params.setDelaiAnnulationHeures(request.getDelaiAnnulationHeures());
        params.setPenaliteAnnulation(request.getPenaliteAnnulation());
        params.setDelaiRemboursementJours(request.getDelaiRemboursementJours());
        params.setCommissionAgence(request.getCommissionAgence());
        params.setTimeoutVerrouillageMinutes(request.getTimeoutVerrouillageMinutes());
        params.setDevisePrincipale(request.getDevisePrincipale());
        params.setDeviseSecondaire(request.getDeviseSecondaire());
        params.setTauxConversion(request.getTauxConversion());
        params.setUpdatedAt(LocalDateTime.now());

        parametresRepository.save(params);

        return mapToDto(params);
    }

    /**
     * Récupère les paramètres existants ou en crée un par défaut si aucun
     */
    private ParametresGeneraux getOrCreateParams() {
        // Récupérer le premier enregistrement (il n'y en a qu'un)
        return parametresRepository.findAll().stream()
                .findFirst()
                .orElseGet(this::createDefaultParams);
    }

    /**
     * Crée les paramètres par défaut
     */
    private ParametresGeneraux createDefaultParams() {
        ParametresGeneraux params = new ParametresGeneraux();
        params.setDelaiAnnulationHeures(24);
        params.setPenaliteAnnulation(0.5);
        params.setDelaiRemboursementJours(7);
        params.setCommissionAgence(0.1);
        params.setTimeoutVerrouillageMinutes(15);
        params.setDevisePrincipale("CDF");
        params.setDeviseSecondaire("USD");
        params.setTauxConversion(2800.0);
        params.setCreatedAt(LocalDateTime.now());
        params.setUpdatedAt(LocalDateTime.now());

        return parametresRepository.save(params);
    }

    /**
     * Convertit l'entité en DTO
     */
    private ParametresGenerauxDto mapToDto(ParametresGeneraux params) {
        return ParametresGenerauxDto.builder()
                .delaiAnnulationHeures(params.getDelaiAnnulationHeures())
                .penaliteAnnulation(params.getPenaliteAnnulation())
                .delaiRemboursementJours(params.getDelaiRemboursementJours())
                .commissionAgence(params.getCommissionAgence())
                .timeoutVerrouillageMinutes(params.getTimeoutVerrouillageMinutes())
                .devisePrincipale(params.getDevisePrincipale())
                .deviseSecondaire(params.getDeviseSecondaire())
                .tauxConversion(params.getTauxConversion())
                .build();
    }
}