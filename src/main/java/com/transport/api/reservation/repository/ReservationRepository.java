package com.transport.api.reservation.repository;

import com.transport.api.reservation.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.trajetId = :trajetId AND r.status = 'PAID'")
    Long countConfirmedReservationsByTrajetId(@Param("trajetId") Long trajetId);

    @Query("SELECT rs.siegeId FROM ReservationSiege rs WHERE rs.reservationId IN (SELECT r.id FROM Reservation r WHERE r.trajetId = :trajetId AND r.status = 'PAID')")
    List<Long> findReservedSeatIdsByTrajetId(@Param("trajetId") Long trajetId);

    @Query("SELECT CASE WHEN COUNT(rs) > 0 THEN true ELSE false END FROM ReservationSiege rs WHERE rs.reservationId IN (SELECT r.id FROM Reservation r WHERE r.trajetId = :trajetId AND r.status = 'PAID') AND rs.siegeId = :siegeId")
    boolean isSeatReservedForTrajet(@Param("trajetId") Long trajetId, @Param("siegeId") Long siegeId);
}