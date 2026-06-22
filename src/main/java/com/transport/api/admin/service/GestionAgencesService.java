package com.transport.api.admin.service;

import com.transport.api.admin.dto.AgenceAdminDto;
import com.transport.api.admin.dto.CreationAgenceRequest;
import com.transport.api.admin.dto.ModificationAgenceRequest;
import com.transport.api.agence.entity.Agence;
import com.transport.api.agence.repository.AgenceRepository;
import com.transport.api.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GestionAgencesService {

    private final AgenceRepository agenceRepository;

    /**
     * FG4 - Liste des agences
     */
    public List<AgenceAdminDto> getAllAgences() {
        return agenceRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * FG4 - Récupérer une agence par ID
     */
    public AgenceAdminDto getAgenceById(Long id) {
        Agence agence = agenceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agence non trouvée"));
        return mapToDto(agence);
    }

    /**
     * FG4 - Création d'une agence
     */
    @Transactional
    public AgenceAdminDto createAgence(CreationAgenceRequest request) {
        // Vérifier si une agence avec le même nom existe déjà
        if (agenceRepository.existsByName(request.getName())) {
            throw new RuntimeException("Une agence avec ce nom existe déjà");
        }

        Agence agence = new Agence();
        agence.setName(request.getName());
        agence.setAddress(request.getAddress());
        agence.setPhone(request.getPhone());
        agence.setEmail(request.getEmail());
        agence.setCompanyId(1L); // V1: une seule compagnie
        agence.setIsActive(true);
        agence.setCreatedAt(LocalDateTime.now());
        agence.setUpdatedAt(LocalDateTime.now());

        agenceRepository.save(agence);
        return mapToDto(agence);
    }

    /**
     * FG4 - Modification d'une agence
     */
    @Transactional
    public AgenceAdminDto updateAgence(Long id, ModificationAgenceRequest request) {
        Agence agence = agenceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agence non trouvée"));

        agence.setName(request.getName());
        agence.setAddress(request.getAddress());
        agence.setPhone(request.getPhone());
        agence.setEmail(request.getEmail());
        agence.setIsActive(request.getIsActive());
        agence.setUpdatedAt(LocalDateTime.now());

        agenceRepository.save(agence);
        return mapToDto(agence);
    }

    /**
     * FG4 - Suppression d'une agence (vérifier qu'elle n'a pas de dépendances)
     */
    @Transactional
    public String deleteAgence(Long id) {
        Agence agence = agenceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agence non trouvée"));

        // TODO: Vérifier qu'il n'y a pas d'utilisateurs ou réservations associés

        agenceRepository.delete(agence);
        return "Agence supprimée avec succès";
    }

    private AgenceAdminDto mapToDto(Agence agence) {
        return AgenceAdminDto.builder()
                .id(agence.getId())
                .name(agence.getName())
                .address(agence.getAddress())
                .phone(agence.getPhone())
                .email(agence.getEmail())
                .companyId(agence.getCompanyId())
                .isActive(agence.getIsActive())
                .createdAt(agence.getCreatedAt())
                .updatedAt(agence.getUpdatedAt())
                .build();
    }
}