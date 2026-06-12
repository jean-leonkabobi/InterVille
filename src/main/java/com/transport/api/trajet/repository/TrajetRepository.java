package com.transport.api.trajet.repository;

import com.transport.api.trajet.entity.Trajet;
import com.transport.api.trajet.enums.StatutTrajet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TrajetRepository extends JpaRepository<Trajet, Long> {

    List<Trajet> findByLigneIdAndDepartureTimeBetween(Long ligneId, LocalDateTime start, LocalDateTime end);

    List<Trajet> findByCompanyIdAndDepartureTimeBetweenOrderByDepartureTimeAsc(Long companyId, LocalDateTime start, LocalDateTime end);

    Optional<Trajet> findByIdAndCompanyId(Long id, Long companyId);

    List<Trajet> findByCompanyId(Long companyId);

    List<Trajet> findByChauffeurIdAndDepartureTimeBetween(Long chauffeurId, LocalDateTime start, LocalDateTime end);

    boolean existsByBusIdAndDepartureTimeBetween(Long busId, LocalDateTime start, LocalDateTime end);

    boolean existsByChauffeurIdAndDepartureTimeBetween(Long chauffeurId, LocalDateTime start, LocalDateTime end);

    @Query("SELECT t FROM Trajet t WHERE t.companyId = :companyId " +
            "AND t.departureTime BETWEEN :start AND :end " +
            "AND t.status != 'CANCELLED' " +
            "AND EXISTS (SELECT l FROM Ligne l WHERE l.id = t.ligneId " +
            "AND LOWER(l.departureCity) LIKE LOWER(CONCAT('%', :departure, '%')) " +
            "AND LOWER(l.arrivalCity) LIKE LOWER(CONCAT('%', :arrival, '%'))) " +
            "ORDER BY t.departureTime ASC")
    List<Trajet> searchTrajets(@Param("companyId") Long companyId,
                               @Param("departure") String departure,
                               @Param("arrival") String arrival,
                               @Param("start") LocalDateTime start,
                               @Param("end") LocalDateTime end);
}