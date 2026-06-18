package com.transport.api.reservation.service;

import com.transport.api.bus.entity.Siege;
import com.transport.api.bus.repository.SiegeRepository;
import com.transport.api.common.exception.ResourceNotFoundException;
import com.transport.api.common.utils.QrCodeGenerator;
import com.transport.api.reservation.dto.TicketDto;
import com.transport.api.reservation.dto.TicketValidationRequest;
import com.transport.api.reservation.dto.TicketValidationResponse;
import com.transport.api.reservation.entity.Reservation;
import com.transport.api.reservation.entity.ReservationSiege;
import com.transport.api.reservation.entity.Ticket;
import com.transport.api.reservation.enums.StatutReservation;
import com.transport.api.reservation.enums.StatutTicket;
import com.transport.api.reservation.repository.ReservationRepository;
import com.transport.api.reservation.repository.ReservationSiegeRepository;
import com.transport.api.reservation.repository.TicketRepository;
import com.transport.api.trajet.entity.Ligne;
import com.transport.api.trajet.entity.Trajet;
import com.transport.api.trajet.repository.LigneRepository;
import com.transport.api.trajet.repository.TrajetRepository;
import com.transport.api.user.entity.User;
import com.transport.api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationSiegeRepository reservationSiegeRepository;
    private final TrajetRepository trajetRepository;
    private final LigneRepository ligneRepository;
    private final SiegeRepository siegeRepository;
    private final UserRepository userRepository;
    private final QrCodeGenerator qrCodeGenerator;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Transactional
    public TicketDto generateTicket(Long reservationId) {
        // 1. Vérifier que la réservation existe et est payée
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Réservation non trouvée"));

        if (reservation.getStatus() != StatutReservation.PAID && reservation.getStatus() != StatutReservation.PENDING) {
            throw new RuntimeException("Seules les réservations payées ou en attente peuvent générer un ticket");
        }

        // 2. Vérifier si un ticket existe déjà
        if (ticketRepository.findByReservationId(reservationId).isPresent()) {
            throw new RuntimeException("Un ticket existe déjà pour cette réservation");
        }

        // 3. Récupérer les informations pour le QR Code
        Trajet trajet = trajetRepository.findById(reservation.getTrajetId())
                .orElseThrow(() -> new ResourceNotFoundException("Trajet non trouvé"));

        Ligne ligne = ligneRepository.findById(trajet.getLigneId())
                .orElseThrow(() -> new ResourceNotFoundException("Ligne non trouvée"));

        // Récupérer les sièges
        String seatNumbers = reservationSiegeRepository.findByReservationId(reservationId)
                .stream()
                .map(rs -> {
                    Siege siege = siegeRepository.findById(rs.getSiegeId()).orElse(null);
                    return siege != null ? siege.getSeatNumber() : "?";
                })
                .reduce((a, b) -> a + "," + b)
                .orElse("?");

        // 4. Construire le contenu du QR Code
        String qrContent = String.format("%s|%d|%s|%s|%s",
                reservation.getReservationCode().toString(),
                trajet.getId(),
                seatNumbers,
                trajet.getDepartureTime().toString(),
                reservation.getPassengerName() != null ? reservation.getPassengerName() : ""
        );

        // 5. Générer l'image QR Code
        String qrCodeImage = qrCodeGenerator.generateQrCodeBase64(qrContent, 300, 300);

        // 6. Créer le ticket
        Ticket ticket = new Ticket();
        ticket.setReservationId(reservationId);
        ticket.setQrCode(reservation.getReservationCode().toString());
        ticket.setQrCodeImage(qrCodeImage);
        ticket.setStatus(StatutTicket.ISSUED);
        ticket.setCreatedAt(LocalDateTime.now());

        Ticket saved = ticketRepository.save(ticket);

        return mapToDto(saved);
    }

    public TicketDto getTicketByReservationId(Long reservationId) {
        Ticket ticket = ticketRepository.findByReservationId(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket non trouvé pour cette réservation"));
        return mapToDto(ticket);
    }

    public TicketDto getTicketByQrCode(String qrCode) {
        Ticket ticket = ticketRepository.findByQrCode(qrCode)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket non trouvé"));
        return mapToDto(ticket);
    }

    @Transactional
    public TicketValidationResponse validateTicket(TicketValidationRequest request) {
        // 1. Trouver le ticket
        Ticket ticket = ticketRepository.findByQrCode(request.getQrCode())
                .orElseThrow(() -> new ResourceNotFoundException("Ticket invalide"));

        // 2. Vérifier s'il n'est pas déjà validé
        if (ticket.getStatus() == StatutTicket.VALIDATED) {
            return TicketValidationResponse.builder()
                    .valid(false)
                    .message("Ce ticket a déjà été utilisé")
                    .build();
        }

        // 3. Vérifier si le trajet n'est pas passé
        Reservation reservation = reservationRepository.findById(ticket.getReservationId())
                .orElseThrow(() -> new ResourceNotFoundException("Réservation non trouvée"));

        Trajet trajet = trajetRepository.findById(reservation.getTrajetId())
                .orElseThrow(() -> new ResourceNotFoundException("Trajet non trouvé"));

        if (trajet.getDepartureTime().isBefore(LocalDateTime.now())) {
            return TicketValidationResponse.builder()
                    .valid(false)
                    .message("Le trajet est déjà parti")
                    .build();
        }

        // 4. Valider le ticket
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User validator = userRepository.findByEmail(email).orElse(null);

        ticket.setStatus(StatutTicket.VALIDATED);
        ticket.setValidatedAt(LocalDateTime.now());
        if (validator != null) {
            ticket.setValidatedBy(validator.getId());
        }
        ticketRepository.save(ticket);

        // 5. Récupérer les infos pour la réponse
        Ligne ligne = ligneRepository.findById(trajet.getLigneId())
                .orElseThrow(() -> new ResourceNotFoundException("Ligne non trouvée"));

        String seatNumbers = reservationSiegeRepository.findByReservationId(reservation.getId())
                .stream()
                .map(rs -> {
                    Siege siege = siegeRepository.findById(rs.getSiegeId()).orElse(null);
                    return siege != null ? siege.getSeatNumber() : "?";
                })
                .reduce((a, b) -> a + "," + b)
                .orElse("?");

        return TicketValidationResponse.builder()
                .valid(true)
                .message("Ticket validé avec succès")
                .passengerName(reservation.getPassengerName())
                .departureCity(ligne.getDepartureCity())
                .arrivalCity(ligne.getArrivalCity())
                .departureTime(trajet.getDepartureTime().format(DATE_FORMATTER))
                .seatNumber(seatNumbers)
                .build();
    }

    private TicketDto mapToDto(Ticket ticket) {
        return TicketDto.builder()
                .id(ticket.getId())
                .reservationId(ticket.getReservationId())
                .qrCode(ticket.getQrCode())
                .qrCodeImage(ticket.getQrCodeImage())
                .validatedAt(ticket.getValidatedAt())
                .validatedBy(ticket.getValidatedBy())
                .status(ticket.getStatus())
                .createdAt(ticket.getCreatedAt())
                .build();
    }
}