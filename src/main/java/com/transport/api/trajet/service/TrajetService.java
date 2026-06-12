package com.transport.api.trajet.service;

import com.transport.api.bus.entity.Bus;
import com.transport.api.bus.repository.BusRepository;
import com.transport.api.common.exception.ResourceNotFoundException;
import com.transport.api.context.TenantContext;
import com.transport.api.reservation.repository.ReservationRepository;
import com.transport.api.trajet.dto.*;
import com.transport.api.trajet.entity.Ligne;
import com.transport.api.trajet.entity.Trajet;
import com.transport.api.trajet.enums.StatutTrajet;
import com.transport.api.trajet.repository.LigneRepository;
import com.transport.api.trajet.repository.TrajetRepository;
import com.transport.api.user.entity.User;
import com.transport.api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TrajetService {

    private final TrajetRepository trajetRepository;
    private final LigneRepository ligneRepository;
    private final BusRepository busRepository;
    private final UserRepository userRepository;
    private final ReservationRepository reservationRepository;

    @Transactional
    public TrajetDto createTrajet(TrajetCreateRequest request) {
        Long companyId = TenantContext.getCurrentTenant();

        // Vérifier que la ligne existe
        Ligne ligne = ligneRepository.findByIdAndCompanyId(request.getLigneId(), companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Ligne non trouvée"));

        // Vérifier que le bus existe
        Bus bus = busRepository.findByIdAndCompanyId(request.getBusId(), companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Bus non trouvé"));

        // Vérifier que le chauffeur existe (si fourni)
        if (request.getChauffeurId() != null) {
            User chauffeur = userRepository.findByIdAndCompanyId(request.getChauffeurId(), companyId)
                    .orElseThrow(() -> new ResourceNotFoundException("Chauffeur non trouvé"));
        }

        // Vérifier les conflits d'horaire pour le bus
        if (trajetRepository.existsByBusIdAndDepartureTimeBetween(
                request.getBusId(),
                request.getDepartureTime().minusHours(2),
                request.getDepartureTime().plusHours(2))) {
            throw new RuntimeException("Ce bus est déjà planifié sur un autre trajet à cette heure");
        }

        // Vérifier les conflits d'horaire pour le chauffeur
        if (request.getChauffeurId() != null && trajetRepository.existsByChauffeurIdAndDepartureTimeBetween(
                request.getChauffeurId(),
                request.getDepartureTime().minusHours(2),
                request.getDepartureTime().plusHours(2))) {
            throw new RuntimeException("Ce chauffeur est déjà assigné à un autre trajet à cette heure");
        }

        Trajet trajet = new Trajet();
        trajet.setLigneId(request.getLigneId());
        trajet.setBusId(request.getBusId());
        trajet.setChauffeurId(request.getChauffeurId());
        trajet.setDepartureTime(request.getDepartureTime());
        trajet.setArrivalTime(request.getArrivalTime());
        trajet.setBasePrice(request.getBasePrice());
        trajet.setStatus(StatutTrajet.SCHEDULED);
        trajet.setCompanyId(companyId);

        Trajet saved = trajetRepository.save(trajet);
        return mapToDto(saved);
    }

    public List<TrajetDto> getAllTrajets() {
        Long companyId = TenantContext.getCurrentTenant();
        return trajetRepository.findByCompanyId(companyId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public TrajetDto getTrajetById(Long id) {
        Long companyId = TenantContext.getCurrentTenant();
        Trajet trajet = trajetRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Trajet non trouvé"));
        return mapToDto(trajet);
    }

    public List<TrajetResultDto> searchTrajets(RechercheTrajetRequest request) {
        Long companyId = TenantContext.getCurrentTenant();

        LocalDateTime startOfDay = request.getDate().atStartOfDay();
        LocalDateTime endOfDay = request.getDate().atTime(LocalTime.MAX);

        List<Trajet> trajets = trajetRepository.searchTrajets(
                companyId,
                request.getDepartureCity(),
                request.getArrivalCity(),
                startOfDay,
                endOfDay
        );

        return trajets.stream()
                .map(this::mapToResultDto)
                .collect(Collectors.toList());
    }

    public List<TrajetResultDto> searchTrajetsPublic(String departure, String arrival, LocalDate date) {
        // Version publique sans authentification (pour clients)
        // En V1, on utilise companyId = 1 par défaut
        Long companyId = 1L;

        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        List<Trajet> trajets = trajetRepository.searchTrajets(
                companyId,
                departure,
                arrival,
                startOfDay,
                endOfDay
        );

        return trajets.stream()
                .map(this::mapToResultDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public TrajetDto updateTrajet(Long id, TrajetCreateRequest request) {
        Long companyId = TenantContext.getCurrentTenant();
        Trajet trajet = trajetRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Trajet non trouvé"));

        // Vérifier que la ligne existe
        ligneRepository.findByIdAndCompanyId(request.getLigneId(), companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Ligne non trouvée"));

        // Vérifier que le bus existe
        busRepository.findByIdAndCompanyId(request.getBusId(), companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Bus non trouvé"));

        trajet.setLigneId(request.getLigneId());
        trajet.setBusId(request.getBusId());
        trajet.setChauffeurId(request.getChauffeurId());
        trajet.setDepartureTime(request.getDepartureTime());
        trajet.setArrivalTime(request.getArrivalTime());
        trajet.setBasePrice(request.getBasePrice());

        Trajet updated = trajetRepository.save(trajet);
        return mapToDto(updated);
    }

    @Transactional
    public void deleteTrajet(Long id) {
        Long companyId = TenantContext.getCurrentTenant();
        Trajet trajet = trajetRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Trajet non trouvé"));
        trajetRepository.delete(trajet);
    }

    @Transactional
    public void updateTrajetStatus(Long id, StatutTrajet status) {
        Long companyId = TenantContext.getCurrentTenant();
        Trajet trajet = trajetRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Trajet non trouvé"));
        trajet.setStatus(status);
        trajetRepository.save(trajet);
    }

    private TrajetDto mapToDto(Trajet trajet) {
        Ligne ligne = ligneRepository.findById(trajet.getLigneId()).orElse(null);
        Bus bus = busRepository.findById(trajet.getBusId()).orElse(null);

        Integer availableSeats = calculateAvailableSeats(trajet.getId(), bus != null ? bus.getCapacity() : 0);

        return TrajetDto.builder()
                .id(trajet.getId())
                .ligneId(trajet.getLigneId())
                .ligneName(ligne != null ? ligne.getDepartureCity() + " → " + ligne.getArrivalCity() : null)
                .busId(trajet.getBusId())
                .busRegistration(bus != null ? bus.getRegistration() : null)
                .chauffeurId(trajet.getChauffeurId())
                .departureTime(trajet.getDepartureTime())
                .arrivalTime(trajet.getArrivalTime())
                .basePrice(trajet.getBasePrice())
                .status(trajet.getStatus())
                .availableSeats(availableSeats)
                .build();
    }

    private TrajetResultDto mapToResultDto(Trajet trajet) {
        Ligne ligne = ligneRepository.findById(trajet.getLigneId()).orElse(null);
        Bus bus = busRepository.findById(trajet.getBusId()).orElse(null);

        Integer availableSeats = calculateAvailableSeats(trajet.getId(), bus != null ? bus.getCapacity() : 0);

        return TrajetResultDto.builder()
                .id(trajet.getId())
                .ligneId(trajet.getLigneId())
                .departureCity(ligne != null ? ligne.getDepartureCity() : null)
                .arrivalCity(ligne != null ? ligne.getArrivalCity() : null)
                .departureTime(trajet.getDepartureTime())
                .arrivalTime(trajet.getArrivalTime())
                .durationMinutes(ligne != null ? (int)(ligne.getDurationSeconds() / 60) : null)
                .price(trajet.getBasePrice())
                .availableSeats(availableSeats)
                .totalSeats(bus != null ? bus.getCapacity() : 0)
                .status(trajet.getStatus())
                .build();
    }

    private Integer calculateAvailableSeats(Long trajetId, Integer totalSeats) {
        if (totalSeats == null) return 0;
        Long reservedCount = reservationRepository.countConfirmedReservationsByTrajetId(trajetId);
        return totalSeats - (reservedCount != null ? reservedCount.intValue() : 0);
    }
}