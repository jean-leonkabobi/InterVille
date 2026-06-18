package com.transport.api.ticket.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.transport.api.bus.entity.Siege;
import com.transport.api.bus.repository.SiegeRepository;
import com.transport.api.paiement.entity.Transaction;
import com.transport.api.paiement.repository.TransactionRepository;
import com.transport.api.reservation.entity.Reservation;
import com.transport.api.reservation.entity.ReservationSiege;
import com.transport.api.reservation.entity.Ticket;
import com.transport.api.reservation.enums.StatutReservation;
import com.transport.api.reservation.repository.ReservationRepository;
import com.transport.api.reservation.repository.ReservationSiegeRepository;
import com.transport.api.reservation.repository.TicketRepository;
import com.transport.api.ticket.dto.TicketPrintDto;
import com.transport.api.trajet.entity.Ligne;
import com.transport.api.trajet.entity.Trajet;
import com.transport.api.trajet.repository.LigneRepository;
import com.transport.api.trajet.repository.TrajetRepository;
import com.transport.api.user.entity.User;
import com.transport.api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TicketPrintService {

    private final ReservationRepository reservationRepository;
    private final TicketRepository ticketRepository;
    private final TrajetRepository trajetRepository;
    private final LigneRepository ligneRepository;
    private final SiegeRepository siegeRepository;
    private final ReservationSiegeRepository reservationSiegeRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final String COMPANY_NAME = "Transport RDC";
    private static final String COMPANY_ADDRESS = "Kinshasa, RDC";
    private static final String COMPANY_PHONE = "+243 812 345 678";
    private static final String CURRENCY = "CDF";

    /**
     * Récupère les données pour l'impression d'un ticket
     */
    public TicketPrintDto getTicketPrintData(Long reservationId) {
        // 1. Récupérer la réservation
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Réservation non trouvée"));

        if (reservation.getStatus() != StatutReservation.PAID) {
            throw new RuntimeException("Seules les réservations payées peuvent être imprimées");
        }

        // 2. Récupérer le ticket
        Ticket ticket = ticketRepository.findByReservationId(reservationId)
                .orElseThrow(() -> new RuntimeException("Ticket non trouvé pour cette réservation"));

        // 3. Récupérer le trajet
        Trajet trajet = trajetRepository.findById(reservation.getTrajetId())
                .orElseThrow(() -> new RuntimeException("Trajet non trouvé"));

        Ligne ligne = ligneRepository.findById(trajet.getLigneId())
                .orElseThrow(() -> new RuntimeException("Ligne non trouvée"));

        // 4. Récupérer les sièges
        List<ReservationSiege> reservationSieges = reservationSiegeRepository.findByReservationId(reservationId);
        String seatNumbers = reservationSieges.stream()
                .map(rs -> {
                    Siege siege = siegeRepository.findById(rs.getSiegeId()).orElse(null);
                    return siege != null ? siege.getSeatNumber() : "?";
                })
                .collect(Collectors.joining(", "));

        // 5. Récupérer le paiement
        List<Transaction> transactions = transactionRepository.findByReservationId(reservationId);
        Transaction transaction = transactions.isEmpty() ? null : transactions.get(0);

        // 6. Récupérer l'agent
        String agentName = "Agent";
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User agent = userRepository.findByEmail(email).orElse(null);
        if (agent != null) {
            agentName = agent.getFullName();
        }

        // 7. Générer le QR Code en Base64
        String qrContent = String.format("%s|%s|%s|%s",
                reservation.getReservationCode().toString(),
                seatNumbers,
                trajet.getDepartureTime().toString(),
                reservation.getPassengerName()
        );
        String qrCodeBase64 = generateQrCodeBase64(qrContent);

        // 8. Calculer la durée
        String duration = formatDuration(ligne.getDurationSeconds());

        return TicketPrintDto.builder()
                .companyName(COMPANY_NAME)
                .companyAddress(COMPANY_ADDRESS)
                .companyPhone(COMPANY_PHONE)
                .reservationCode(reservation.getReservationCode().toString())
                .passengerName(reservation.getPassengerName())
                .passengerPhone(reservation.getPassengerPhone())
                .seatNumber(seatNumbers)
                .departureCity(ligne.getDepartureCity())
                .arrivalCity(ligne.getArrivalCity())
                .departureTime(trajet.getDepartureTime().format(DATE_FORMATTER))
                .arrivalTime(trajet.getArrivalTime().format(DATE_FORMATTER))
                .duration(duration)
                .price(String.valueOf(reservation.getTotalPrice()))
                .currency(CURRENCY)
                .paymentMethod(transaction != null ? transaction.getPaymentMode().name() : "N/A")
                .paymentReference(transaction != null ? transaction.getMobileMoneyRef() : "N/A")
                .qrCodeBase64(qrCodeBase64)
                .qrCodeText(qrContent)
                .printedAt(LocalDateTime.now().format(DATE_FORMATTER))
                .agentName(agentName)
                .build();
    }

    /**
     * Génère un QR Code en Base64
     */
    private String generateQrCodeBase64(String content) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, 200, 200);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);

            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (Exception e) {
            log.error("Erreur lors de la génération du QR Code", e);
            return "";
        }
    }

    /**
     * Formate la durée en texte lisible
     */
    private String formatDuration(Long seconds) {
        if (seconds == null) return "0 minute";

        long days = seconds / 86400;
        long hours = (seconds % 86400) / 3600;
        long minutes = (seconds % 3600) / 60;

        StringBuilder result = new StringBuilder();
        if (days > 0) {
            result.append(days).append(" j");
        }
        if (hours > 0) {
            if (result.length() > 0) result.append(" ");
            result.append(hours).append("h");
        }
        if (minutes > 0) {
            if (result.length() > 0) result.append(" ");
            result.append(minutes).append("min");
        }
        return result.length() > 0 ? result.toString() : "0min";
    }

    /**
     * Génère le texte du ticket pour impression
     * Format 80mm
     */
    public String generateTicketText(TicketPrintDto data) {
        StringBuilder sb = new StringBuilder();
        String separator = "================================\n";

        sb.append(separator);
        sb.append("       ").append(data.getCompanyName()).append("\n");
        sb.append(separator);
        sb.append("         BILLET DE VOYAGE\n");
        sb.append(separator);

        sb.append("Code    : ").append(data.getReservationCode()).append("\n");
        sb.append("Agent   : ").append(data.getAgentName()).append("\n");
        sb.append("Date    : ").append(data.getPrintedAt()).append("\n");
        sb.append(separator);

        sb.append(data.getDepartureCity()).append(" → ").append(data.getArrivalCity()).append("\n");
        sb.append("Départ  : ").append(data.getDepartureTime()).append("\n");
        sb.append("Arrivée : ").append(data.getArrivalTime()).append("\n");
        sb.append("Durée   : ").append(data.getDuration()).append("\n");
        sb.append(separator);

        sb.append("Siège   : ").append(data.getSeatNumber()).append("\n");
        sb.append("Prix    : ").append(data.getPrice()).append(" ").append(data.getCurrency()).append("\n");
        sb.append(separator);

        sb.append("Passager: ").append(data.getPassengerName()).append("\n");
        sb.append("Tél     : ").append(data.getPassengerPhone()).append("\n");
        sb.append(separator);

        sb.append("QR Code : ").append(data.getQrCodeText()).append("\n");

        // Ajouter le QR Code en ASCII art (optionnel)
        sb.append("\n");
        sb.append("   [ QR CODE À IMPRIMER ]\n");
        sb.append("\n");

        sb.append(separator);
        sb.append("    Merci pour votre voyage !\n");
        sb.append(separator);

        return sb.toString();
    }
}