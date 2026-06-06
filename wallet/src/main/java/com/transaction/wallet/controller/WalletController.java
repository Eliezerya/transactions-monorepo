package com.transaction.wallet.controller;

import com.transaction.wallet.model.dto.UserDto;
import com.transaction.wallet.model.dto.WalletDto;
import com.transaction.wallet.service.AuthService;
import com.transaction.wallet.service.WalletService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/wallet")
public class WalletController {

    @Autowired
    private AuthService authService;

    @Autowired
    private WalletService walletService;

    @PostMapping("")
    public ResponseEntity<?> createWallet(@RequestHeader("Authorization") String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
        }
        UserDto userInfo = authService.validateToken(authorizationHeader);
        long userId = userInfo.getUserId();

        WalletDto newWallet = WalletDto.builder()
                .userId(userId)
                .balance(0.00)
                .currency("IDR")
                .createdAt(Instant.now().toString())
                .updatedAt(Instant.now().toString())
                .build();

        walletService.createWallet(userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(newWallet);
    }

    @GetMapping("")
    public ResponseEntity<?> getUserWalletByUserId(@RequestHeader("Authorization") String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
        }
        UserDto userInfo = authService.validateToken(authorizationHeader);
        int userId = userInfo.getUserId();

        WalletDto wallet = walletService.getWalletByUserId((long) userId);
        return ResponseEntity.status(HttpStatus.OK).body(wallet);
    }
}
