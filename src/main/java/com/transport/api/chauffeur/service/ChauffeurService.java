package com.transport.api.chauffeur.service;

import com.transport.api.bus.entity.Bus;
import com.transport.api.bus.entity.Siege;
import com.transport.api.bus.repository.BusRepository;
import com.transport.api.bus.repository.SiegeRepository;
import com.transport.api.chauffeur.dto.ManifesteDto;
import com.transport.api.chauffeur.dto.MissionDto;
import com.transport.api.chauffeur.dto.ValidationManuelleRequest;
import com.transport.api.reservation.entity.Reservation;
import com.transport.api.reservation.entity.ReservationSiege;
import com.transport.api.reservation.entity.Ticket;
import com.transport.api.reservation.enums.StatutTicket;
import com.transport.api.reservation.repository.ReservationRepository;
import com.transport.api.reservation.repository.ReservationSiegeRepository;
import com.transport.api.reservation.repository.TicketRepository;
import com.transport.api.trajet.entity.Trajet;
import com.transport.api.trajet.enums.StatutTrajet;
import com.transport.api.trajet.repository.TrajetRepository;
import com.transport.api.user.entity.User;
import com.transport.api.user.repository.UserRepository;
import jakarta.transaction.Transactional;
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
    private final SiegeRepository siegeRepository;
    private final ReservationSiegeRepository reservationSiegeRepository;
    private final TicketRepository ticketRepository;

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

    /**
     * FD3 - Manifeste des passagers pour un trajet
     */
    public ManifesteDto getManifeste(Long trajetId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User chauffeur = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Chauffeur non trouvé"));

        Trajet trajet = trajetRepository.findById(trajetId)
                .orElseThrow(() -> new RuntimeException("Trajet non trouvé"));

        // Vérifier que le trajet est bien assigné au chauffeur
        if (!trajet.getChauffeurId().equals(chauffeur.getId())) {
            throw new RuntimeException("Ce trajet ne vous est pas assigné");
        }

        Bus bus = busRepository.findById(trajet.getBusId()).orElse(null);

        // Récupérer les réservations du trajet (uniquement payées)
        List<Reservation> reservations = reservationRepository.findPaidReservationsByTrajetId(trajetId);

        List<ManifesteDto.PassagerDto> passagers = reservations.stream()
                .map(reservation -> {
                    // Récupérer les sièges
                    List<ReservationSiege> sieges = reservationSiegeRepository.findByReservationId(reservation.getId());
                    String siegeNumbers = sieges.stream()
                            .map(rs -> {
                                Siege siege = siegeRepository.findById(rs.getSiegeId()).orElse(null);
                                return siege != null ? siege.getSeatNumber() : "?";
                            })
                            .collect(Collectors.joining(", "));

                    // Récupérer le ticket
                    Ticket ticket = ticketRepository.findByReservationId(reservation.getId()).orElse(null);

                    return ManifesteDto.PassagerDto.builder()
                            .nom(reservation.getPassengerName())
                            .telephone(reservation.getPassengerPhone())
                            .siege(siegeNumbers)
                            .embarque(ticket != null && ticket.getStatus() == StatutTicket.VALIDATED)
                            .qrCode(ticket != null ? ticket.getQrCode() : null)
                            .build();
                })
                .collect(Collectors.toList());

        String departureCity = "";
        String arrivalCity = "";
        if (trajet.getLigne() != null) {
            departureCity = trajet.getLigne().getDepartureCity();
            arrivalCity = trajet.getLigne().getArrivalCity();
        }

        return ManifesteDto.builder()
                .trajetId(trajetId)
                .departureCity(departureCity)
                .arrivalCity(arrivalCity)
                .departureTime(trajet.getDepartureTime().toString())
                .arrivalTime(trajet.getArrivalTime().toString())
                .busRegistration(bus != null ? bus.getRegistration() : "N/A")
                .totalPassagers(passagers.size())
                .passagers(passagers)
                .build();
    }

    /**
     * FD5 - Validation manuelle d'un passager (sans scan)
     */
    @Transactional
    public String validationManuelle(ValidationManuelleRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User chauffeur = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Chauffeur non trouvé"));

        // Vérifier que le trajet est assigné au chauffeur
        Trajet trajet = trajetRepository.findById(request.getTrajetId())
                .orElseThrow(() -> new RuntimeException("Trajet non trouvé"));

        if (!trajet.getChauffeurId().equals(chauffeur.getId())) {
            throw new RuntimeException("Ce trajet ne vous est pas assigné");
        }

        // Chercher la réservation correspondante
        Reservation reservation = reservationRepository.findByTrajetIdAndPassengerName(
                        request.getTrajetId(), request.getPassengerName())
                .orElseThrow(() -> new RuntimeException("Passager non trouvé sur ce trajet"));

        // Vérifier si déjà embarqué
        Ticket ticket = ticketRepository.findByReservationId(reservation.getId()).orElse(null);
        if (ticket != null && ticket.getStatus() == StatutTicket.VALIDATED) {
            throw new RuntimeException("Ce passager a déjà embarqué");
        }

        // Valider manuellement
        if (ticket == null) {
            // Créer un ticket si pas encore généré
            ticket = new Ticket();
            ticket.setReservationId(reservation.getId());
            ticket.setQrCode(reservation.getReservationCode().toString());
            ticket.setStatus(StatutTicket.ISSUED);
            ticket.setCreatedAt(LocalDateTime.now());
            ticket = ticketRepository.save(ticket);
        }

        ticket.setStatus(StatutTicket.VALIDATED);
        ticket.setValidatedAt(LocalDateTime.now());
        ticket.setValidatedBy(chauffeur.getId());
        ticketRepository.save(ticket);

        return "Passager " + request.getPassengerName() + " validé avec succès";
    }

    /**
     * FD6 - Mise à jour du statut du trajet
     */
    @Transactional
    public String updateStatutTrajet(Long trajetId, StatutTrajet nouveauStatut) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User chauffeur = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Chauffeur non trouvé"));

        Trajet trajet = trajetRepository.findById(trajetId)
                .orElseThrow(() -> new RuntimeException("Trajet non trouvé"));

        if (!trajet.getChauffeurId().equals(chauffeur.getId())) {
            throw new RuntimeException("Ce trajet ne vous est pas assigné");
        }

        trajet.setStatus(nouveauStatut);
        trajet.setUpdatedAt(LocalDateTime.now());
        trajetRepository.save(trajet);

        return "Statut du trajet mis à jour avec succès: " + nouveauStatut.name();
    }

    /**
     * FD7 - Historique des missions (30 jours)
     */
    public List<MissionDto> getHistoriqueMissions() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User chauffeur = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Chauffeur non trouvé"));

        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);

        List<Trajet> trajets = trajetRepository.findByChauffeurIdAndDepartureTimeBetween(
                chauffeur.getId(), thirtyDaysAgo, LocalDateTime.now());

        return trajets.stream()
                .map(this::mapToMissionDto)
                .collect(Collectors.toList());
    }
}