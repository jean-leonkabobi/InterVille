package com.transport.api.reservation.service;

import com.transport.api.bus.entity.Siege;
import com.transport.api.bus.repository.SiegeRepository;
import com.transport.api.common.exception.ResourceNotFoundException;
import com.transport.api.paiement.entity.Transaction;
import com.transport.api.paiement.repository.TransactionRepository;
import com.transport.api.reservation.dto.ClientTicketDto;
import com.transport.api.reservation.dto.TicketDetailDto;
import com.transport.api.reservation.entity.Reservation;
import com.transport.api.reservation.entity.ReservationSiege;
import com.transport.api.reservation.entity.Ticket;
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

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClientTicketService {

    private final ReservationRepository reservationRepository;
    private final ReservationSiegeRepository reservationSiegeRepository;
    private final TicketRepository ticketRepository;
    private final TransactionRepository transactionRepository;
    private final TrajetRepository trajetRepository;
    private final LigneRepository ligneRepository;
    private final SiegeRepository siegeRepository;
    private final UserRepository userRepository;

    /**
     * FC10 - Historique des billets du client connecté
     */
    public List<ClientTicketDto> getMesTickets() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        List<Reservation> reservations = reservationRepository.findByUserIdOrderByCreatedAtDesc(user.getId());

        return reservations.stream()
                .map(this::mapToClientTicketDto)
                .collect(Collectors.toList());
    }

    /**
     * FC10 - Historique des billets avec filtre par statut
     */
    public List<ClientTicketDto> getMesTicketsByStatus(String status) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        List<Reservation> reservations = reservationRepository.findByUserIdAndStatus(user.getId(), status);

        return reservations.stream()
                .map(this::mapToClientTicketDto)
                .collect(Collectors.toList());
    }

    /**
     * FC11 - Détails d'un billet spécifique
     */
    public TicketDetailDto getTicketDetail(Long reservationId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Réservation non trouvée"));

        // Vérifier que le ticket appartient bien à l'utilisateur
        if (!reservation.getUserId().equals(user.getId())) {
            throw new RuntimeException("Accès non autorisé à ce billet");
        }

        // Infos trajet
        Trajet trajet = trajetRepository.findById(reservation.getTrajetId())
                .orElseThrow(() -> new ResourceNotFoundException("Trajet non trouvé"));

        Ligne ligne = ligneRepository.findById(trajet.getLigneId())
                .orElseThrow(() -> new ResourceNotFoundException("Ligne non trouvée"));

        // Infos sièges
        List<ReservationSiege> reservationSieges = reservationSiegeRepository.findByReservationId(reservationId);
        List<String> sieges = reservationSieges.stream()
                .map(rs -> {
                    Siege siege = siegeRepository.findById(rs.getSiegeId()).orElse(null);
                    return siege != null ? siege.getSeatNumber() : "?";
                })
                .collect(Collectors.toList());

        // Infos ticket
        Ticket ticket = ticketRepository.findByReservationId(reservationId).orElse(null);

        // Infos paiement
        Transaction transaction = transactionRepository.findByReservationId(reservationId)
                .stream()
                .findFirst()
                .orElse(null);

        // Formater la durée
        String durationFormatted = formatDuration(ligne.getDurationSeconds());

        return TicketDetailDto.builder()
                .id(reservation.getId())
                .reservationCode(reservation.getReservationCode().toString())
                .reservationStatus(reservation.getStatus())
                .reservationDate(reservation.getCreatedAt())
                .totalPrice(reservation.getTotalPrice().doubleValue())
                .trajetId(trajet.getId())
                .departureCity(ligne.getDepartureCity())
                .arrivalCity(ligne.getArrivalCity())
                .departureTime(trajet.getDepartureTime())
                .arrivalTime(trajet.getArrivalTime())
                .durationFormatted(durationFormatted)
                .sieges(sieges)
                .passengerName(reservation.getPassengerName())
                .passengerPhone(reservation.getPassengerPhone())
                .ticketStatus(ticket != null ? ticket.getStatus() : null)
                .qrCode(ticket != null ? ticket.getQrCode() : null)
                .qrCodeImage(ticket != null ? ticket.getQrCodeImage() : null)
                .validatedAt(ticket != null ? ticket.getValidatedAt() : null)
                .validatedBy(ticket != null && ticket.getValidatedBy() != null ?
                        userRepository.findById(ticket.getValidatedBy()).map(User::getFullName).orElse(null) : null)
                .paymentMode(transaction != null ? transaction.getPaymentMode() : null)
                .paymentReference(transaction != null ? transaction.getMobileMoneyRef() : null)
                .paymentDate(transaction != null ? transaction.getPaymentDate() : null)
                .createdAt(reservation.getCreatedAt())
                .build();
    }

    private ClientTicketDto mapToClientTicketDto(Reservation reservation) {
        Trajet trajet = trajetRepository.findById(reservation.getTrajetId()).orElse(null);
        Ligne ligne = trajet != null ? ligneRepository.findById(trajet.getLigneId()).orElse(null) : null;
        Ticket ticket = ticketRepository.findByReservationId(reservation.getId()).orElse(null);

        List<ReservationSiege> reservationSieges = reservationSiegeRepository.findByReservationId(reservation.getId());
        List<String> sieges = reservationSieges.stream()
                .map(rs -> {
                    Siege siege = siegeRepository.findById(rs.getSiegeId()).orElse(null);
                    return siege != null ? siege.getSeatNumber() : "?";
                })
                .collect(Collectors.toList());

        return ClientTicketDto.builder()
                .id(reservation.getId())
                .reservationCode(reservation.getReservationCode().toString())
                .departureCity(ligne != null ? ligne.getDepartureCity() : null)
                .arrivalCity(ligne != null ? ligne.getArrivalCity() : null)
                .departureTime(trajet != null ? trajet.getDepartureTime() : null)
                .sieges(sieges)
                .totalPrice(reservation.getTotalPrice().doubleValue())
                .reservationStatus(reservation.getStatus())
                .ticketStatus(ticket != null ? ticket.getStatus() : null)
                .qrCode(ticket != null ? ticket.getQrCode() : null)
                .validatedAt(ticket != null ? ticket.getValidatedAt() : null)
                .createdAt(reservation.getCreatedAt())
                .build();
    }

    private String formatDuration(Long seconds) {
        if (seconds == null) return "0 minute";

        long days = seconds / 86400;
        long hours = (seconds % 86400) / 3600;
        long minutes = (seconds % 3600) / 60;

        StringBuilder result = new StringBuilder();
        if (days > 0) {
            result.append(days).append(" jour").append(days > 1 ? "s" : "");
            if (hours > 0 || minutes > 0) result.append(" ");
        }
        if (hours > 0) {
            result.append(hours).append(" heure").append(hours > 1 ? "s" : "");
            if (minutes > 0) result.append(" ");
        }
        if (minutes > 0) {
            result.append(minutes).append(" minute").append(minutes > 1 ? "s" : "");
        }
        return result.length() > 0 ? result.toString() : "0 minute";
    }
}