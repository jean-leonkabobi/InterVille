package com.transport.api.reservation.repository;

import com.transport.api.paiement.entity.Transaction;
import com.transport.api.reservation.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
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

    @Query("SELECT rs.siegeId FROM ReservationSiege rs WHERE rs.reservationId IN (SELECT r.id FROM Reservation r WHERE r.trajetId = :trajetId AND r.status = 'PAID')")
    List<Long> findReservedSeatIdsByTrajetId(@Param("trajetId") Long trajetId);

    @Query("SELECT CASE WHEN COUNT(rs) > 0 THEN true ELSE false END FROM ReservationSiege rs WHERE rs.reservationId IN (SELECT r.id FROM Reservation r WHERE r.trajetId = :trajetId AND r.status = 'PAID') AND rs.siegeId = :siegeId")
    boolean isSeatReservedForTrajet(@Param("trajetId") Long trajetId, @Param("siegeId") Long siegeId);

    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.trajetId = :trajetId AND r.status = 'PAID'")
    Long countConfirmedReservationsByTrajetId(@Param("trajetId") Long trajetId);

    @Query("SELECT r FROM Reservation r WHERE r.userId = :userId AND r.status = :status ORDER BY r.createdAt DESC")
    List<Reservation> findByUserIdAndStatus(@Param("userId") Long userId, @Param("status") String status);

    List<Reservation> findByAgenceIdAndCreatedAtBetween(Long agenceId, LocalDateTime start, LocalDateTime end);

    @Query("SELECT r FROM Reservation r WHERE r.trajetId = :trajetId AND r.status = 'PAID'")
    List<Reservation> findPaidReservationsByTrajetId(@Param("trajetId") Long trajetId);

    Long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT COALESCE(AVG(r.totalPrice), 0) FROM Reservation r WHERE r.status = 'PAID'")
    Double averageTauxRemplissage();

    Optional<Reservation> findByTrajetIdAndPassengerName(Long trajetId, String passengerName);

    List<Reservation> findByCreatedAtBetweenAndStatus(LocalDateTime start, LocalDateTime end, String status);

    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.id IN :transactionIds")
    Long countPassagersByTransactions(@Param("transactionIds") List<Transaction> transactions);
}