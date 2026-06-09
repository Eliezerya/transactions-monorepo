package com.transaction.wallet.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletDto {
    private Long id;
    private long userId;
    private Double balance;
    private String currency;
    private String createdAt;
    private String updatedAt;
}
