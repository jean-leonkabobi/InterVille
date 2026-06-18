package com.transport.api.agence.service;

import com.transport.api.agence.dto.AgenceRequest;
import com.transport.api.agence.dto.AgenceResponse;
import com.transport.api.agence.entity.Agence;
import com.transport.api.agence.repository.AgenceRepository;
import com.transport.api.common.exception.ResourceNotFoundException;
import com.transport.api.context.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AgenceService {

    private final AgenceRepository agenceRepository;

    @Transactional
    public AgenceResponse createAgence(AgenceRequest request) {
        Long companyId = TenantContext.getCurrentTenant();

        Agence agence = new Agence();
        agence.setName(request.getName());
        agence.setAddress(request.getAddress());
        agence.setPhone(request.getPhone());
        agence.setCompanyId(companyId);

        Agence saved = agenceRepository.save(agence);
        return mapToResponse(saved);
    }

    public List<AgenceResponse> getAllAgences() {
        Long companyId = TenantContext.getCurrentTenant();
        return agenceRepository.findByCompanyId(companyId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public AgenceResponse getAgenceById(Long id) {
        Agence agence = findAgenceById(id);
        return mapToResponse(agence);
    }

    @Transactional
    public AgenceResponse updateAgence(Long id, AgenceRequest request) {
        Agence agence = findAgenceById(id);
        agence.setName(request.getName());
        agence.setAddress(request.getAddress());
        agence.setPhone(request.getPhone());

        Agence updated = agenceRepository.save(agence);
        return mapToResponse(updated);
    }

    @Transactional
    public void deleteAgence(Long id) {
        Agence agence = findAgenceById(id);
        agenceRepository.delete(agence);
    }

    private Agence findAgenceById(Long id) {
        Long companyId = TenantContext.getCurrentTenant();
        return agenceRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Agence non trouvée avec l'id: " + id));
    }

    private AgenceResponse mapToResponse(Agence agence) {
        return AgenceResponse.builder()
                .id(agence.getId())
                .name(agence.getName())
                .address(agence.getAddress())
                .phone(agence.getPhone())
                .companyId(agence.getCompanyId())
                .createdAt(agence.getCreatedAt())
                .build();
    }
}