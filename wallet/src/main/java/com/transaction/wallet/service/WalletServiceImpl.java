package com.transaction.wallet.service;

import com.transaction.wallet.model.dto.WalletDto;
import com.transaction.wallet.model.entity.Wallet;
import com.transaction.wallet.repository.WalletRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class WalletServiceImpl implements WalletService {

    @Autowired
    private WalletRepository walletRepository;

    @Override
    public WalletDto createWallet(Long userId) {
        if (walletRepository.existsByUserId(userId)) {
            throw new RuntimeException("Wallet already exists for user: " + userId);
        }
        Wallet wallet = new Wallet();
        wallet.setUserId(userId);
        walletRepository.save(wallet);

        return WalletDto.builder()
                .userId(userId != null ? userId.intValue() : 0)
                .balance(wallet.getBalance())
                .currency(wallet.getCurrency())
                .createdAt(wallet.getCreatedAt().toString())
                .updatedAt(wallet.getUpdatedAt().toString())
                .build();
    }

    @Override
    public WalletDto getWalletByUserId(Long userId) {
        return walletRepository.findByUserId(userId)
                .map(Wallet::toDto)
                .orElseThrow(() -> new RuntimeException("Wallet not found for user: " + userId));
    }
}
