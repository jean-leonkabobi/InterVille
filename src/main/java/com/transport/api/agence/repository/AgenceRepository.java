package com.transport.api.agence.repository;

import com.transport.api.agence.entity.Agence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AgenceRepository extends JpaRepository<Agence, Long> {

    List<Agence> findByCompanyId(Long companyId);
}