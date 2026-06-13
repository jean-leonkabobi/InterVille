package com.transport.api.bus.service;

import com.transport.api.bus.dto.BusCreateRequest;
import com.transport.api.bus.dto.BusDto;
import com.transport.api.bus.entity.Bus;
import com.transport.api.bus.entity.Siege;
import com.transport.api.bus.enums.StatutBus;
import com.transport.api.bus.enums.TypeSiege;
import com.transport.api.bus.repository.BusRepository;
import com.transport.api.bus.repository.SiegeRepository;
import com.transport.api.context.TenantContext;
import com.transport.api.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BusService {

    private final BusRepository busRepository;
    private final SiegeRepository siegeRepository;

    @Transactional
    public BusDto createBus(BusCreateRequest request) {
        Bus bus = new Bus();
        bus.setRegistration(request.getRegistration());
        bus.setCapacity(request.getCapacity());
        bus.setSeatConfig(request.getSeatConfig() != null ? request.getSeatConfig() : "{}");
        bus.setStatus(StatutBus.OPERATIONAL);
        bus.setCompanyId(TenantContext.getCurrentTenant());

        Bus saved = busRepository.save(bus);

        // Créer automatiquement les sièges pour ce bus
        createSiegesForBus(saved.getId(), saved.getCapacity());

        return mapToDto(saved);
    }

    /**
     * Crée automatiquement les sièges pour un bus basé sur sa capacité
     * Configuration: 5 sièges par rangée (A, B, C, D, E...)
     */
    private void createSiegesForBus(Long busId, int capacity) {
        List<Siege> sieges = new ArrayList<>();
        Long companyId = TenantContext.getCurrentTenant();

        // Lettres pour les rangées (A à Z, max 26 rangées)
        char[] rows = {'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z'};

        int seatsCreated = 0;
        int maxColumns = 2; // 2 sièges par rangée (ex: A1, A2)

        for (char row : rows) {
            for (int col = 1; col <= maxColumns; col++) {
                if (seatsCreated >= capacity) {
                    break;
                }

                Siege siege = new Siege();
                siege.setBusId(busId);
                siege.setSeatNumber(String.valueOf(row) + col);
                siege.setSeatType(TypeSiege.STANDARD);
                siege.setPositionX(seatsCreated / maxColumns);
                siege.setPositionY(col);
                siege.setCompanyId(companyId);
                sieges.add(siege);
                seatsCreated++;
            }
            if (seatsCreated >= capacity) {
                break;
            }
        }

        siegeRepository.saveAll(sieges);
    }

    @Transactional
    public BusDto updateBus(Long id, BusCreateRequest request) {
        Bus bus = findBusById(id);
        bus.setRegistration(request.getRegistration());
        bus.setCapacity(request.getCapacity());
        bus.setSeatConfig(request.getSeatConfig() != null ? request.getSeatConfig() : "{}");

        Bus updated = busRepository.save(bus);

        // Si la capacité a augmenté, ajouter les nouveaux sièges
        int currentSeatsCount = siegeRepository.countByBusId(id);
        if (updated.getCapacity() > currentSeatsCount) {
            int additionalSeats = updated.getCapacity() - currentSeatsCount;
            addAdditionalSiegesForBus(id, currentSeatsCount, additionalSeats);
        }

        return mapToDto(updated);
    }

    /**
     * Ajoute des sièges supplémentaires quand la capacité du bus augmente
     */
    private void addAdditionalSiegesForBus(Long busId, int startIndex, int additionalCount) {
        List<Siege> sieges = new ArrayList<>();
        Long companyId = TenantContext.getCurrentTenant();

        char[] rows = {'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z'};
        int maxColumns = 2;

        int seatsToAdd = additionalCount;
        for (char row : rows) {
            for (int col = 1; col <= maxColumns; col++) {
                if (seatsToAdd <= 0) {
                    break;
                }

                Siege siege = new Siege();
                siege.setBusId(busId);
                siege.setSeatNumber(String.valueOf(row) + col);
                siege.setSeatType(TypeSiege.STANDARD);
                siege.setPositionX(startIndex / maxColumns);
                siege.setPositionY(col);
                siege.setCompanyId(companyId);
                sieges.add(siege);
                seatsToAdd--;
                startIndex++;
            }
            if (seatsToAdd <= 0) {
                break;
            }
        }

        siegeRepository.saveAll(sieges);
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
    public void deleteBus(Long id) {
        Bus bus = findBusById(id);
        // Supprimer d'abord les sièges associés
        siegeRepository.deleteByBusId(id);
        // Puis supprimer le bus
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