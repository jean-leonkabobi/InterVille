package com.transport.api.reservation.repository;

import com.transport.api.reservation.entity.VerrouSiege;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VerrouSiegeRepository extends JpaRepository<VerrouSiege, Long> {

    Optional<VerrouSiege> findByTrajetIdAndSiegeId(Long trajetId, Long siegeId);

    List<VerrouSiege> findByTrajetId(Long trajetId);

    List<VerrouSiege> findByExpiresAtBefore(LocalDateTime now);

    @Modifying
    @Query("DELETE FROM VerrouSiege v WHERE v.expiresAt < :now")
    int deleteAllExpired(@Param("now") LocalDateTime now);

    @Modifying
    @Query("DELETE FROM VerrouSiege v WHERE v.trajetId = :trajetId AND v.reservationSessionId = :sessionId")
    void deleteByTrajetIdAndSessionId(@Param("trajetId") Long trajetId, @Param("sessionId") UUID sessionId);

    boolean existsByTrajetIdAndSiegeIdAndExpiresAtAfter(Long trajetId, Long siegeId, LocalDateTime now);
}