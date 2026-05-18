package org.example.bankingtransactionapi.repository;

import org.example.bankingtransactionapi.model.BankAccount;
import org.example.bankingtransactionapi.model.Transaction;
import org.example.bankingtransactionapi.model.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    @Query("""
            SELECT t FROM Transaction t
            WHERE (t.fromAccount = :account OR t.toAccount = :account)
            AND (:type IS NULL OR t.transactionType = :type)
            AND (:startDate IS NULL OR t.createdAt >= :startDate)
            AND (:endDate IS NULL OR t.createdAt <= :endDate)
            ORDER BY t.createdAt DESC
            """)
    Page<Transaction> findByAccountAndFilters(
            @Param("account") BankAccount account,
            @Param("type") TransactionType type,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);
}
