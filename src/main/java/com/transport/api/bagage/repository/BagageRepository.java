package com.transport.api.bagage.repository;

import com.transport.api.bagage.entity.Bagage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BagageRepository extends JpaRepository<Bagage, Long> {
}