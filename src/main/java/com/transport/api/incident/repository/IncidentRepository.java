package com.transport.api.incident.repository;

import com.transport.api.incident.entity.Incident;
import com.transport.api.incident.enums.StatutIncident;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IncidentRepository extends JpaRepository<Incident, Long> {
    List<Incident> findByStatus(StatutIncident status);
    List<Incident> findByTrajetId(Long trajetId);
    Long countByStatus(StatutIncident status);
}