package com.transaction.wallet.service;

import com.transaction.wallet.model.dto.MonthlyReportDto;
import com.transaction.wallet.model.dto.TranscationDto;
import com.transaction.wallet.model.dto.TransferResponseDto;

public interface TransactionService {
    TransferResponseDto transfer(Long senderUserId, TranscationDto request);

    MonthlyReportDto monthlyTransaction(Long userId, int month, int year, String type, String status, Double minAmount);
}
