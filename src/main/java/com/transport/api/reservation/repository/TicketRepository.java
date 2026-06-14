package com.transport.api.reservation.repository;

import com.transport.api.reservation.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    Optional<Ticket> findByQrCode(String qrCode);

    Optional<Ticket> findByReservationId(Long reservationId);
}