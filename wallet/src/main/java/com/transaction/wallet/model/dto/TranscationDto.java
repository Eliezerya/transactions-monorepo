package com.transaction.wallet.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TranscationDto {
    private Long id;
    private Long walletId;
    private Long counterpartyWalletId;
    private Long recipientWalletId;
    private Double amount;
    private String type;
    private String status;
    private String referenceNumber;
    private String description;
    private String createdAt;
}
