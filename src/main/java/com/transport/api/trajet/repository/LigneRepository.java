package com.transport.api.trajet.repository;

import com.transport.api.trajet.entity.Ligne;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LigneRepository extends JpaRepository<Ligne, Long> {
    List<Ligne> findByCompanyId(Long companyId);
    Optional<Ligne> findByIdAndCompanyId(Long id, Long companyId);
}