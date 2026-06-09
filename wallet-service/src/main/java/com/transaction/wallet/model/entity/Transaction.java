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

@Table(name = "transactions")
@Entity
@Getter
@Setter
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "wallet_id", nullable = false)
    private Long walletId;

    @Column(name = "counterparty_wallet_id", nullable = false)
    private Long counterpartyWalletId;

    @Column(nullable = false)
    private Double amount;

    @Column(nullable = false, length = 20)
    private String type; // TRANSFER_OUT or TRANSFER_IN

    @Column(nullable = false, length = 20)
    private String status; // PENDING, SUCCESS, FAILED

    @Column(name = "reference_number", unique = true, nullable = false, length = 50)
    private String referenceNumber;

    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
}
