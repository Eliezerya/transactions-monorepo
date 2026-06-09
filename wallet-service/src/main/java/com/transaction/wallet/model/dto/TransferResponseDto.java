package com.transaction.wallet.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferResponseDto {
    private String status;
    private String referenceNumber;
    private Double amount;
    private Long senderWalletId;
    private Long recipientWalletId;
    private String description;
    private String timestamp;
}
