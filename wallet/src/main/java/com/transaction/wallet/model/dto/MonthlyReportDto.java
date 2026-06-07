package com.transaction.wallet.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyReportDto {
    private Long walletId;
    private double totalVolume;
    private String currency;
    private List<TranscationDto> transactions;
    private Map<String, Double> volumeByType;
}
