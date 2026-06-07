package com.transaction.wallet.model.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;
import com.transaction.wallet.model.dto.WalletDto;

@Table(name = "wallets")
@Entity
@Getter
@Setter
public class Wallet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", unique = true, nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Double balance = 0.00;

    @Column(nullable = false, length = 3)
    private String currency = "IDR";

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at")
    private Instant updatedAt = Instant.now();

    public void debit(double amount) {
        if (balance < amount) {
            throw new RuntimeException("Insufficient balance");
        }
        balance -= amount;
    }

    public void credit(double amount) {
        balance += amount;
    }

    public WalletDto toDto() {
        return WalletDto.builder()
                .userId(userId != null ? userId : 0L)
                .balance(balance)
                .currency(currency)
                .createdAt(createdAt != null ? createdAt.toString() : null)
                .updatedAt(updatedAt != null ? updatedAt.toString() : null)
                .build();
    }
}