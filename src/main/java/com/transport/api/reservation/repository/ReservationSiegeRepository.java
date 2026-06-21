package com.transport.api.reservation.repository;

import com.transport.api.reservation.entity.ReservationSiege;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservationSiegeRepository extends JpaRepository<ReservationSiege, Long> {

    List<ReservationSiege> findByReservationId(Long reservationId);

    @Query("SELECT rs.siegeId FROM ReservationSiege rs WHERE rs.reservationId IN (SELECT r.id FROM Reservation r WHERE r.trajetId = :trajetId AND r.status = 'PAID')")
    List<Long> findReservedSeatIdsByTrajetId(@Param("trajetId") Long trajetId);

    @Modifying
    @Query("DELETE FROM ReservationSiege rs WHERE rs.reservationId = :reservationId")
    void deleteByReservationId(@Param("reservationId") Long reservationId);

    @Query("SELECT COUNT(rs) FROM ReservationSiege rs WHERE rs.reservationId IN (SELECT r.id FROM Reservation r WHERE r.trajetId = :trajetId AND r.status = 'PAID')")
    Long countReservedSeatsByTrajetId(@Param("trajetId") Long trajetId);
}