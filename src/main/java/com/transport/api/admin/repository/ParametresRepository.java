package com.transport.api.admin.repository;

import com.transport.api.admin.entity.ParametresGeneraux;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ParametresRepository extends JpaRepository<ParametresGeneraux, Long> {
}