package com.transport.api.bus.service;

import com.transport.api.bus.dto.BusCreateRequest;
import com.transport.api.bus.dto.BusDto;
import com.transport.api.bus.entity.Bus;
import com.transport.api.bus.enums.StatutBus;
import com.transport.api.bus.repository.BusRepository;
import com.transport.api.context.TenantContext;
import com.transport.api.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BusService {

    private final BusRepository busRepository;

    @Transactional
    public BusDto createBus(BusCreateRequest request) {
        Bus bus = new Bus();
        bus.setRegistration(request.getRegistration());
        bus.setCapacity(request.getCapacity());
        bus.setSeatConfig(request.getSeatConfig());
        bus.setStatus(StatutBus.OPERATIONAL);
        bus.setCompanyId(TenantContext.getCurrentTenant());

        Bus saved = busRepository.save(bus);
        return mapToDto(saved);
    }

    public List<BusDto> getAllBuses() {
        Long companyId = TenantContext.getCurrentTenant();
        return busRepository.findByCompanyId(companyId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public BusDto getBusById(Long id) {
        Bus bus = findBusById(id);
        return mapToDto(bus);
    }

    @Transactional
    public BusDto updateBus(Long id, BusCreateRequest request) {
        Bus bus = findBusById(id);
        bus.setRegistration(request.getRegistration());
        bus.setCapacity(request.getCapacity());
        bus.setSeatConfig(request.getSeatConfig());

        Bus updated = busRepository.save(bus);
        return mapToDto(updated);
    }

    @Transactional
    public void deleteBus(Long id) {
        Bus bus = findBusById(id);
        busRepository.delete(bus);
    }

    private Bus findBusById(Long id) {
        Long companyId = TenantContext.getCurrentTenant();
        return busRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Bus non trouvé avec l'id: " + id));
    }

    private BusDto mapToDto(Bus bus) {
        return BusDto.builder()
                .id(bus.getId())
                .registration(bus.getRegistration())
                .capacity(bus.getCapacity())
                .seatConfig(bus.getSeatConfig())
                .status(bus.getStatus())
                .companyId(bus.getCompanyId())
                .build();
    }
}