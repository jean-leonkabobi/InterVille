package com.transport.api.paiement.repository;

import com.transport.api.paiement.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByReservationId(Long reservationId);

    List<Transaction> findByStatus(String status);

    List<Transaction> findByAgenceIdAndDateBetween(Long agenceId, LocalDateTime start, LocalDateTime end);
}