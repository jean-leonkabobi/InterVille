package com.transport.api.bus.repository;

import com.transport.api.bus.entity.Bus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BusRepository extends JpaRepository<Bus, Long> {

    Optional<Bus> findByRegistration(String registration);

    Optional<Bus> findByRegistrationAndCompanyId(String registration, Long companyId);

    List<Bus> findByCompanyId(Long companyId);

    Optional<Bus> findByIdAndCompanyId(Long id, Long companyId);
}