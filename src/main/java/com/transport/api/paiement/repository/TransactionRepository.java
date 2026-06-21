package com.transport.api.paiement.repository;

import com.transport.api.paiement.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByReservationId(Long reservationId);

    List<Transaction> findByStatus(String status);

    List<Transaction> findByAgenceIdAndCreatedAtBetween(Long agenceId, LocalDateTime start, LocalDateTime end);

    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.status = 'SUCCESS'")
    Double sumAllSuccessAmounts();

    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.status = 'SUCCESS' AND t.paymentDate BETWEEN :start AND :end")
    Double sumSuccessAmountsByDate(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}