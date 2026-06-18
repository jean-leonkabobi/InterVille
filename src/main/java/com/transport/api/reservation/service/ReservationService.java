package com.transport.api.reservation.service;

import com.transport.api.bagage.entity.Bagage;
import com.transport.api.bagage.repository.BagageRepository;
import com.transport.api.bus.entity.Bus;
import com.transport.api.bus.entity.Siege;
import com.transport.api.bus.repository.BusRepository;
import com.transport.api.bus.repository.SiegeRepository;
import com.transport.api.common.exception.ResourceNotFoundException;
import com.transport.api.context.TenantContext;
import com.transport.api.reservation.dto.ModificationReservationRequest;
import com.transport.api.reservation.dto.ReservationAgentRequest;
import com.transport.api.reservation.dto.ReservationRequest;
import com.transport.api.reservation.dto.ReservationResponse;
import com.transport.api.reservation.entity.Reservation;
import com.transport.api.reservation.entity.ReservationSiege;
import com.transport.api.reservation.entity.VerrouSiege;
import com.transport.api.reservation.enums.StatutReservation;
import com.transport.api.reservation.repository.ReservationRepository;
import com.transport.api.reservation.repository.ReservationSiegeRepository;
import com.transport.api.reservation.repository.VerrouSiegeRepository;
import com.transport.api.trajet.entity.Ligne;
import com.transport.api.trajet.entity.Trajet;
import com.transport.api.trajet.repository.LigneRepository;
import com.transport.api.trajet.repository.TrajetRepository;
import com.transport.api.user.entity.User;
import com.transport.api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationSiegeRepository reservationSiegeRepository;
    private final VerrouSiegeRepository verrouSiegeRepository;
    private final TrajetRepository trajetRepository;
    private final LigneRepository ligneRepository;
    private final BusRepository busRepository;
    private final SiegeRepository siegeRepository;
    private final UserRepository userRepository;
    private final BagageRepository bagageRepository;
    private final TicketService ticketService;

    @Value("${reservation.verrouillage.timeout-minutes:15}")
    private long timeoutMinutes;

    /**
     * Récupère l'ID de l'agence de l'agent connecté
     */
    private Long getCurrentAgenceId() {
        try {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();

            if (email == null || email.equals("anonymousUser")) {
                throw new RuntimeException("Utilisateur non authentifié");
            }

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé: " + email));

            Long agenceId = user.getAgenceId();
            if (agenceId == null) {
                throw new RuntimeException("Cet utilisateur n'est pas associé à une agence");
            }

            return agenceId;
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la récupération de l'agence: " + e.getMessage());
        }
    }

    @Transactional
    public ReservationResponse createReservation(ReservationRequest request) {
        Long companyId = TenantContext.getCurrentTenant();

        // 1. Récupérer le trajet
        Trajet trajet = trajetRepository.findById(request.getTrajetId())
                .orElseThrow(() -> new ResourceNotFoundException("Trajet non trouvé"));

        Ligne ligne = ligneRepository.findById(trajet.getLigneId())
                .orElseThrow(() -> new ResourceNotFoundException("Ligne non trouvée"));

        Bus bus = busRepository.findById(trajet.getBusId())
                .orElseThrow(() -> new ResourceNotFoundException("Bus non trouvé"));

        // 2. Vérifier que tous les sièges sont verrouillés par cette session
        List<Siege> sieges = new ArrayList<>();
        for (String numero : request.getNumerosSieges()) {
            Siege siege = siegeRepository.findByBusIdAndSeatNumber(bus.getId(), numero)
                    .orElseThrow(() -> new ResourceNotFoundException("Siège non trouvé: " + numero));

            VerrouSiege verrou = verrouSiegeRepository.findByTrajetIdAndSiegeId(
                    trajet.getId(), siege.getId()).orElse(null);

            if (verrou == null) {
                throw new RuntimeException("Le siège " + numero + " n'est pas verrouillé");
            }

            if (!verrou.getReservationSessionId().equals(request.getSessionId())) {
                throw new RuntimeException("Le siège " + numero + " est verrouillé par une autre session");
            }

            if (verrou.getExpiresAt().isBefore(LocalDateTime.now())) {
                throw new RuntimeException("Le verrou du siège " + numero + " a expiré");
            }

            sieges.add(siege);
        }

        // 3. Vérifier que les sièges ne sont pas déjà réservés
        List<Long> reservedSeats = reservationSiegeRepository.findReservedSeatIdsByTrajetId(trajet.getId());
        for (Siege siege : sieges) {
            if (reservedSeats.contains(siege.getId())) {
                throw new RuntimeException("Le siège " + siege.getSeatNumber() + " est déjà réservé");
            }
        }

        // 4. Calculer le prix total
        java.math.BigDecimal totalPrice = java.math.BigDecimal.valueOf(trajet.getBasePrice())
                .multiply(java.math.BigDecimal.valueOf(request.getNumerosSieges().size()));

        // 5. Récupérer l'utilisateur connecté (si client)
        Long userId = null;
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        if (email != null && !email.equals("anonymousUser")) {
            User user = userRepository.findByEmail(email).orElse(null);
            if (user != null) {
                userId = user.getId();
            }
        }

        // 6. Créer la réservation
        Reservation reservation = new Reservation();
        reservation.setCompanyId(companyId);
        reservation.setUserId(userId);
        reservation.setTrajetId(trajet.getId());
        reservation.setReservationCode(UUID.randomUUID());
        reservation.setPassengerName(request.getPassengerName());
        reservation.setPassengerPhone(request.getPassengerPhone());
        reservation.setStatus(StatutReservation.PENDING);
        reservation.setTotalPrice(totalPrice);
        reservation.setExpiresAt(LocalDateTime.now().plusMinutes(timeoutMinutes));

        Reservation saved = reservationRepository.save(reservation);

        // 7. Associer les sièges à la réservation
        for (Siege siege : sieges) {
            ReservationSiege reservationSiege = new ReservationSiege();
            reservationSiege.setReservationId(saved.getId());
            reservationSiege.setSiegeId(siege.getId());
            reservationSiege.setTrajetId(trajet.getId());
            reservationSiegeRepository.save(reservationSiege);
        }

        // 8. Supprimer les verrous (transformés en réservation)
        verrouSiegeRepository.deleteByTrajetIdAndSessionId(trajet.getId(), request.getSessionId());

        // 9. Construire la réponse
        List<String> siegeNumbers = sieges.stream()
                .map(Siege::getSeatNumber)
                .collect(Collectors.toList());

        return ReservationResponse.builder()
                .id(saved.getId())
                .reservationCode(saved.getReservationCode())
                .trajetId(trajet.getId())
                .departureCity(ligne.getDepartureCity())
                .arrivalCity(ligne.getArrivalCity())
                .departureTime(trajet.getDepartureTime())
                .sieges(siegeNumbers)
                .totalPrice(totalPrice)
                .status(saved.getStatus())
                .expiresAt(saved.getExpiresAt())
                .passengerName(request.getPassengerName())
                .passengerPhone(request.getPassengerPhone())
                .build();
    }

    public ReservationResponse getReservationByCode(UUID reservationCode) {
        Reservation reservation = reservationRepository.findByReservationCode(reservationCode)
                .orElseThrow(() -> new ResourceNotFoundException("Réservation non trouvée"));

        Long companyId = TenantContext.getCurrentTenant();
        if (!reservation.getCompanyId().equals(companyId)) {
            throw new RuntimeException("Accès non autorisé");
        }

        Trajet trajet = trajetRepository.findById(reservation.getTrajetId())
                .orElseThrow(() -> new ResourceNotFoundException("Trajet non trouvé"));

        Ligne ligne = ligneRepository.findById(trajet.getLigneId())
                .orElseThrow(() -> new ResourceNotFoundException("Ligne non trouvée"));

        List<ReservationSiege> reservationSieges = reservationSiegeRepository.findByReservationId(reservation.getId());
        List<String> siegeNumbers = reservationSieges.stream()
                .map(rs -> {
                    Siege siege = siegeRepository.findById(rs.getSiegeId()).orElse(null);
                    return siege != null ? siege.getSeatNumber() : "?";
                })
                .collect(Collectors.toList());

        return ReservationResponse.builder()
                .id(reservation.getId())
                .reservationCode(reservation.getReservationCode())
                .trajetId(trajet.getId())
                .departureCity(ligne.getDepartureCity())
                .arrivalCity(ligne.getArrivalCity())
                .departureTime(trajet.getDepartureTime())
                .sieges(siegeNumbers)
                .totalPrice(reservation.getTotalPrice())
                .status(reservation.getStatus())
                .expiresAt(reservation.getExpiresAt())
                .passengerName(reservation.getPassengerName())
                .passengerPhone(reservation.getPassengerPhone())
                .build();
    }

    @Transactional
    public void cancelReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Réservation non trouvée"));

        if (reservation.getStatus() == StatutReservation.PAID) {
            throw new RuntimeException("Impossible d'annuler une réservation déjà payée");
        }

        reservation.setStatus(StatutReservation.CANCELLED);
        reservationRepository.save(reservation);
    }

    /**
     * Réservation par un agent (sans compte client)
     */
    @Transactional
    public ReservationResponse createReservationByAgent(ReservationAgentRequest request) {
        Long companyId = TenantContext.getCurrentTenant();
        Long agenceId = getCurrentAgenceId();

        // Récupérer le trajet
        Trajet trajet = trajetRepository.findById(request.getTrajetId())
                .orElseThrow(() -> new ResourceNotFoundException("Trajet non trouvé"));

        Ligne ligne = ligneRepository.findById(trajet.getLigneId())
                .orElseThrow(() -> new ResourceNotFoundException("Ligne non trouvée"));

        Bus bus = busRepository.findById(trajet.getBusId())
                .orElseThrow(() -> new ResourceNotFoundException("Bus non trouvé"));

        // Vérifier et verrouiller les sièges
        String sessionId = UUID.randomUUID().toString();
        List<Siege> sieges = new ArrayList<>();

        for (String numero : request.getNumerosSieges()) {
            Siege siege = siegeRepository.findByBusIdAndSeatNumber(bus.getId(), numero)
                    .orElseThrow(() -> new ResourceNotFoundException("Siège non trouvé: " + numero));

            // Vérifier si le siège est disponible
            if (reservationRepository.isSeatReservedForTrajet(trajet.getId(), siege.getId())) {
                throw new RuntimeException("Le siège " + numero + " est déjà réservé");
            }
            sieges.add(siege);
        }

        // Calculer le prix total
        double totalPrice = trajet.getBasePrice() * sieges.size();

        // Créer la réservation (sans userId)
        Reservation reservation = new Reservation();
        reservation.setCompanyId(companyId);
        reservation.setUserId(null);  // Pas de compte client
        reservation.setAgenceId(agenceId);
        reservation.setTrajetId(trajet.getId());
        reservation.setReservationCode(UUID.randomUUID());
        reservation.setPassengerName(request.getPassengerName());
        reservation.setPassengerPhone(request.getPassengerPhone());
        reservation.setStatus(StatutReservation.PENDING);
        reservation.setTotalPrice(java.math.BigDecimal.valueOf(totalPrice));
        reservation.setExpiresAt(LocalDateTime.now().plusMinutes(timeoutMinutes));
        reservation.setNumberOfPassengers(1);

        Reservation saved = reservationRepository.save(reservation);

        // Associer les sièges
        for (Siege siege : sieges) {
            ReservationSiege reservationSiege = new ReservationSiege();
            reservationSiege.setReservationId(saved.getId());
            reservationSiege.setSiegeId(siege.getId());
            reservationSiege.setTrajetId(trajet.getId());
            reservationSiegeRepository.save(reservationSiege);
        }

        // Enregistrer les bagages si présents
        if (request.getBagages() != null && request.getBagages() > 0) {
            Bagage bagage = new Bagage();
            bagage.setReservationId(saved.getId());
            bagage.setQuantity(request.getBagages());
            bagage.setLabelPrinted(false);
            bagage.setCreatedAt(LocalDateTime.now());
            bagageRepository.save(bagage);
        }

        // Générer le ticket si demandé
        if (request.getImprimerTicket()) {
            ticketService.generateTicket(saved.getId());
        }

        // Construire la réponse
        List<String> siegeNumbers = sieges.stream()
                .map(Siege::getSeatNumber)
                .collect(Collectors.toList());

        return ReservationResponse.builder()
                .id(saved.getId())
                .reservationCode(saved.getReservationCode())
                .trajetId(trajet.getId())
                .departureCity(ligne.getDepartureCity())
                .arrivalCity(ligne.getArrivalCity())
                .departureTime(trajet.getDepartureTime())
                .sieges(siegeNumbers)
                .totalPrice(java.math.BigDecimal.valueOf(totalPrice))
                .status(saved.getStatus())
                .expiresAt(saved.getExpiresAt())
                .passengerName(request.getPassengerName())
                .passengerPhone(request.getPassengerPhone())
                .build();
    }

    /**
     * FA10 - Consultation d'une réservation
     */
    public ReservationResponse getReservationForAgent(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Réservation non trouvée"));

        // Vérifier que l'agent a accès à cette réservation
        Long agenceId = getCurrentAgenceId();
        if (reservation.getAgenceId() != null && !reservation.getAgenceId().equals(agenceId)) {
            throw new RuntimeException("Accès non autorisé à cette réservation");
        }

        return buildReservationResponse(reservation);
    }

    /**
     * FA10 - Modification d'une réservation
     */
    @Transactional
    public ReservationResponse modifierReservation(Long reservationId, ModificationReservationRequest request) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Réservation non trouvée"));

        // Vérifier que la réservation est modifiable
        if (reservation.getStatus() == StatutReservation.PAID) {
            throw new RuntimeException("Impossible de modifier une réservation déjà payée");
        }

        if (reservation.getStatus() == StatutReservation.CANCELLED) {
            throw new RuntimeException("Impossible de modifier une réservation annulée");
        }

        // Modifier les infos passager
        if (request.getPassengerName() != null) {
            reservation.setPassengerName(request.getPassengerName());
        }
        if (request.getPassengerPhone() != null) {
            reservation.setPassengerPhone(request.getPassengerPhone());
        }

        // Modifier les sièges si demandé
        if (request.getNumerosSieges() != null && !request.getNumerosSieges().isEmpty()) {
            // Libérer les anciens sièges
            reservationSiegeRepository.deleteByReservationId(reservationId);

            // Verrouiller les nouveaux sièges
            Trajet trajet = trajetRepository.findById(reservation.getTrajetId())
                    .orElseThrow(() -> new ResourceNotFoundException("Trajet non trouvé"));
            Bus bus = busRepository.findById(trajet.getBusId())
                    .orElseThrow(() -> new ResourceNotFoundException("Bus non trouvé"));

            List<Siege> sieges = new ArrayList<>();
            for (String numero : request.getNumerosSieges()) {
                Siege siege = siegeRepository.findByBusIdAndSeatNumber(bus.getId(), numero)
                        .orElseThrow(() -> new ResourceNotFoundException("Siège non trouvé: " + numero));

                if (reservationRepository.isSeatReservedForTrajet(trajet.getId(), siege.getId())) {
                    throw new RuntimeException("Le siège " + numero + " est déjà réservé");
                }
                sieges.add(siege);

                ReservationSiege reservationSiege = new ReservationSiege();
                reservationSiege.setReservationId(reservationId);
                reservationSiege.setSiegeId(siege.getId());
                reservationSiege.setTrajetId(trajet.getId());
                reservationSiegeRepository.save(reservationSiege);
            }

            // Recalculer le prix
            double newTotal = trajet.getBasePrice() * sieges.size();
            reservation.setTotalPrice(java.math.BigDecimal.valueOf(newTotal));
        }

        reservation.setUpdatedAt(LocalDateTime.now());
        reservationRepository.save(reservation);

        return buildReservationResponse(reservation);
    }

    /**
     * FA10 - Annulation d'une réservation
     */
    @Transactional
    public String annulerReservation(Long reservationId, String motif) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Réservation non trouvée"));

        if (reservation.getStatus() == StatutReservation.CANCELLED) {
            throw new RuntimeException("Cette réservation est déjà annulée");
        }

        if (reservation.getStatus() == StatutReservation.PAID) {
            // Si payée, il faut gérer le remboursement
            throw new RuntimeException("Cette réservation est payée. Utilisez le remboursement.");
        }

        reservation.setStatus(StatutReservation.CANCELLED);
        reservation.setUpdatedAt(LocalDateTime.now());
        reservationRepository.save(reservation);

        // Libérer les sièges
        // TODO: Libérer les sièges

        return "Réservation annulée avec succès";
    }

    private ReservationResponse buildReservationResponse(Reservation reservation) {
        Trajet trajet = trajetRepository.findById(reservation.getTrajetId())
                .orElseThrow(() -> new ResourceNotFoundException("Trajet non trouvé"));
        Ligne ligne = ligneRepository.findById(trajet.getLigneId())
                .orElseThrow(() -> new ResourceNotFoundException("Ligne non trouvée"));

        List<ReservationSiege> reservationSieges = reservationSiegeRepository.findByReservationId(reservation.getId());
        List<String> siegeNumbers = reservationSieges.stream()
                .map(rs -> {
                    Siege siege = siegeRepository.findById(rs.getSiegeId()).orElse(null);
                    return siege != null ? siege.getSeatNumber() : "?";
                })
                .collect(Collectors.toList());

        return ReservationResponse.builder()
                .id(reservation.getId())
                .reservationCode(reservation.getReservationCode())
                .trajetId(trajet.getId())
                .departureCity(ligne.getDepartureCity())
                .arrivalCity(ligne.getArrivalCity())
                .departureTime(trajet.getDepartureTime())
                .sieges(siegeNumbers)
                .totalPrice(reservation.getTotalPrice())
                .status(reservation.getStatus())
                .expiresAt(reservation.getExpiresAt())
                .passengerName(reservation.getPassengerName())
                .passengerPhone(reservation.getPassengerPhone())
                .build();
    }
}