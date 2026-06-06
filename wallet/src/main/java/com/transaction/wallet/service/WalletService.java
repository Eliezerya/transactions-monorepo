package com.transaction.wallet.service;

import com.transaction.wallet.model.dto.WalletDto;

public interface WalletService {
    public WalletDto createWallet(Long userId);

    public WalletDto getWalletByUserId(Long userId);
}
