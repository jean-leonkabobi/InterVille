package com.transport.api.bus.repository;

import com.transport.api.bus.entity.Siege;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SiegeRepository extends JpaRepository<Siege, Long> {

    List<Siege> findByBusId(Long busId);

    Optional<Siege> findByBusIdAndSeatNumber(Long busId, String seatNumber);

    int countByBusId(Long busId);

    void deleteByBusId(Long busId);
}