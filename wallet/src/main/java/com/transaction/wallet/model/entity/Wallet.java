package com.transaction.wallet.model.entity;

import jakarta.persistence.Entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import com.transaction.wallet.model.dto.WalletDto;

@Table
@Entity(name = "wallet")
@Getter
@Setter
public class Wallet {
    @Column(unique = true)
    @Id
    private Long userId;
    private Double balance = 1000000.00; // just for dummy data, anggap aja bonus registrasi. anyway it should be 0.00
    private String currency = "IDR";
    private Instant createdAt = Instant.now();
    private Instant updatedAt = Instant.now();

    public void debit(double amount) {
        if (balance < amount) {
            throw new RuntimeException("Insufficient balance");
        }
        balance -= amount;
    }

    public WalletDto toDto() {
        return WalletDto.builder()
                .userId(userId != null ? userId.intValue() : 0)
                .balance(balance)
                .currency(currency)
                .createdAt(createdAt != null ? createdAt.toString() : null)
                .updatedAt(updatedAt != null ? updatedAt.toString() : null)
                .build();
    }
}