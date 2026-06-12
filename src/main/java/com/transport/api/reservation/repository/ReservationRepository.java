package com.transport.api.reservation.repository;

import com.transport.api.reservation.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.trajetId = :trajetId AND r.status = 'PAID'")
    Long countConfirmedReservationsByTrajetId(@Param("trajetId") Long trajetId);
}