package com.transport.api.reservation.repository;

import com.transport.api.reservation.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    Optional<Reservation> findByReservationCode(UUID reservationCode);

    @Query("SELECT r FROM Reservation r WHERE r.userId = :userId ORDER BY r.createdAt DESC")
    List<Reservation> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);

    @Query("SELECT r FROM Reservation r WHERE r.trajetId = :trajetId AND r.status IN ('PENDING', 'PAID')")
    List<Reservation> findActiveReservationsByTrajetId(@Param("trajetId") Long trajetId);
}