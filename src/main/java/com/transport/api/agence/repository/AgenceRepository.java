package com.transport.api.agence.repository;

import com.transport.api.agence.entity.Agence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AgenceRepository extends JpaRepository<Agence, Long> {
    List<Agence> findByCompanyId(Long companyId);
    Optional<Agence> findByIdAndCompanyId(Long id, Long companyId);
}