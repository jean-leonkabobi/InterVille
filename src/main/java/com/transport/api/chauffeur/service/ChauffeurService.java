package com.transport.api.chauffeur.service;

import com.transport.api.bus.entity.Bus;
import com.transport.api.bus.repository.BusRepository;
import com.transport.api.chauffeur.dto.MissionDto;
import com.transport.api.reservation.repository.ReservationRepository;
import com.transport.api.trajet.entity.Trajet;
import com.transport.api.trajet.repository.TrajetRepository;
import com.transport.api.user.entity.User;
import com.transport.api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChauffeurService {

    private final TrajetRepository trajetRepository;
    private final UserRepository userRepository;
    private final BusRepository busRepository;
    private final ReservationRepository reservationRepository;

    /**
     * FD2 - Missions du jour pour le chauffeur connecté
     */
    public List<MissionDto> getMissionsDuJour() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User chauffeur = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Chauffeur non trouvé"));

        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = LocalDate.now().atTime(LocalTime.MAX);

        List<Trajet> trajets = trajetRepository.findByChauffeurIdAndDepartureTimeBetween(
                chauffeur.getId(), todayStart, todayEnd);

        return trajets.stream()
                .map(this::mapToMissionDto)
                .collect(Collectors.toList());
    }

    private MissionDto mapToMissionDto(Trajet trajet) {
        Bus bus = busRepository.findById(trajet.getBusId()).orElse(null);

        // Récupérer les places réservées
        Long placesReservees = reservationRepository.countConfirmedReservationsByTrajetId(trajet.getId());
        Integer totalPlaces = bus != null ? bus.getCapacity() : 0;
        Integer placesDisponibles = totalPlaces - placesReservees.intValue();

        // Récupérer les noms des villes
        String departureCity = "";
        String arrivalCity = "";
        try {
            // Si la ligne est chargée via relation
            if (trajet.getLigne() != null) {
                departureCity = trajet.getLigne().getDepartureCity();
                arrivalCity = trajet.getLigne().getArrivalCity();
            }
        } catch (Exception e) {
            // Fallback: utiliser les IDs
        }

        return MissionDto.builder()
                .trajetId(trajet.getId())
                .departureCity(departureCity)
                .arrivalCity(arrivalCity)
                .departureTime(trajet.getDepartureTime())
                .arrivalTime(trajet.getArrivalTime())
                .busRegistration(bus != null ? bus.getRegistration() : "N/A")
                .totalPlaces(totalPlaces)
                .placesReservees(placesReservees.intValue())
                .placesDisponibles(placesDisponibles)
                .statut(trajet.getStatus().name())
                .ligneName(departureCity + " → " + arrivalCity)
                .build();
    }
}