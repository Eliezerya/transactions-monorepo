package com.transaction.wallet.repository;

import com.transaction.wallet.model.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {

    boolean existsByUserId(Long userId);

    Optional<Wallet> findByUserId(Long userId);

    @Query(value = "SELECT * FROM wallets WHERE id = :walletId FOR UPDATE", nativeQuery = true)
    Optional<Wallet> findByIdForUpdate(@Param("walletId") Long walletId);
}