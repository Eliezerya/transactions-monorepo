package com.transaction.wallet.controller;

import com.transaction.wallet.model.dto.UserDto;
import com.transaction.wallet.model.dto.WalletDto;
import com.transaction.wallet.service.AuthService;
import com.transaction.wallet.service.WalletService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wallet")
public class WalletController {

    private final AuthService authService;
    private final WalletService walletService;

    public WalletController(AuthService authService, WalletService walletService) {
        this.authService = authService;
        this.walletService = walletService;
    }

    @PostMapping("")
    public ResponseEntity<?> createWallet(@RequestHeader("Authorization") String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
        }
        UserDto userInfo = authService.validateToken(authorizationHeader);
        long userId = userInfo.getUserId();

        WalletDto wallet = walletService.createWallet(userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(wallet);
    }

    @GetMapping("")
    public ResponseEntity<?> getUserWalletByUserId(@RequestHeader("Authorization") String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
        }
        UserDto userInfo = authService.validateToken(authorizationHeader);
        Long userId = userInfo.getUserId();

        WalletDto wallet = walletService.getWalletByUserId((long) userId);
        return ResponseEntity.status(HttpStatus.OK).body(wallet);
    }
}
