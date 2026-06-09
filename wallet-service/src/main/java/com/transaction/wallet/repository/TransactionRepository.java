package com.transaction.wallet.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.transaction.wallet.model.entity.Transaction;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

        @Query(value = "SELECT * FROM transactions WHERE wallet_id = :walletId AND EXTRACT(MONTH FROM created_at) = :month AND EXTRACT(YEAR FROM created_at) = :year", nativeQuery = true)
        List<Transaction> getMonthlyTransaction(
                        @Param("walletId") Long walletId, @Param("month") int month, @Param("year") int year);

        @Query(value = "SELECT t.* FROM transactions t " +
                        "JOIN wallets w ON (t.wallet_id = w.id OR t.counterparty_wallet_id = w.id) " +
                        "WHERE w.user_id = :userId " +
                        "  AND EXTRACT(MONTH FROM t.created_at) = :month " +
                        "  AND EXTRACT(YEAR FROM t.created_at) = :year " +
                        "  AND (:status IS NULL OR t.status = :status) " +
                        "  AND (:minAmount IS NULL OR t.amount >= :minAmount) " +
                        "  AND (:type IS NULL OR " +
                        "       (CASE WHEN t.counterparty_wallet_id = w.id THEN 'TRANSFER_IN' ELSE t.type END) = :type)", nativeQuery = true)
        List<Transaction> getMonthlyTransactionWithFilters(
                        @Param("userId") Long userId,
                        @Param("month") int month,
                        @Param("year") int year,
                        @Param("type") String type,
                        @Param("status") String status,
                        @Param("minAmount") Double minAmount);
}
