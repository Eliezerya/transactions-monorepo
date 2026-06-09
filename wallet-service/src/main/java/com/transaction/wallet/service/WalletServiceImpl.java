package com.transaction.wallet.service;

import com.transaction.wallet.model.dto.WalletDto;
import com.transaction.wallet.model.entity.Wallet;
import com.transaction.wallet.repository.WalletRepository;
import org.springframework.stereotype.Service;

@Service
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;

    public WalletServiceImpl(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }

    @Override
    public WalletDto createWallet(Long userId) {
        if (walletRepository.existsByUserId(userId)) {
            throw new RuntimeException("Wallet already exists for user: " + userId);
        }
        Wallet wallet = new Wallet();
        wallet.setUserId(userId);
        wallet.setBalance(100000.00); // just for testing data
        walletRepository.save(wallet);

        return WalletDto.builder().id(wallet.getId())
                .userId(wallet.getUserId())
                .balance(wallet.getBalance())
                .currency(wallet.getCurrency())
                .createdAt(wallet.getCreatedAt().toString())
                .updatedAt(wallet.getUpdatedAt().toString())
                .build();
    }

    @Override
    public WalletDto getWalletByUserId(Long userId) {
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Wallet not found for user: " + userId));

        return WalletDto.builder()
                .id(wallet.getId())
                .userId(wallet.getUserId())
                .balance(wallet.getBalance())
                .currency(wallet.getCurrency())
                .createdAt(wallet.getCreatedAt().toString())
                .updatedAt(wallet.getUpdatedAt().toString())
                .build();
    }
}
