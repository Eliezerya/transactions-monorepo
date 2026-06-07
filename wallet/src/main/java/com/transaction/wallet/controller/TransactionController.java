package com.transaction.wallet.controller;

import com.transaction.wallet.model.dto.MonthlyReportDto;
import com.transaction.wallet.model.dto.TranscationDto;
import com.transaction.wallet.model.dto.TransferResponseDto;
import com.transaction.wallet.model.dto.UserDto;
import com.transaction.wallet.service.AuthService;
import com.transaction.wallet.service.TransactionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/transaction")
public class TransactionController {

    private final AuthService authService;
    private final TransactionService transactionService;

    public TransactionController(AuthService authService, TransactionService transactionService) {
        this.authService = authService;
        this.transactionService = transactionService;
    }

    @PostMapping("/transfer")
    public ResponseEntity<?> transfer(@RequestBody TranscationDto request,
            @RequestHeader("Authorization") String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            Map<String, Object> err = new HashMap<>();
            err.put("status", "error");
            err.put("message", "Invalid token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(err);
        }

        UserDto userInfo = authService.validateToken(authorizationHeader);
        if (userInfo == null || !userInfo.isValid()) {
            Map<String, Object> err = new HashMap<>();
            err.put("status", "error");
            err.put("message", "Invalid token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(err);
        }

        try {
            TransferResponseDto response = transactionService.transfer(userInfo.getUserId(), request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "FAILED");
            response.put("message", e.getMessage());
            if (e.getMessage().contains("Recipient wallet not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "FAILED");
            if ("Insufficient balance".equals(e.getMessage())) {
                response.put("message", "Insufficient balance for this transaction");
            } else {
                response.put("message", e.getMessage());
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @GetMapping({ "/monthly" })
    public ResponseEntity<?> monthlyTransaction(@RequestHeader("Authorization") String authorizationHeader,
            @RequestParam(defaultValue = "6") int month,
            @RequestParam(defaultValue = "2026") int year,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Double minAmount) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            Map<String, Object> err = new HashMap<>();
            err.put("status", "error");
            err.put("message", "Invalid token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(err);
        }

        UserDto userInfo = authService.validateToken(authorizationHeader);
        if (userInfo == null || !userInfo.isValid()) {
            Map<String, Object> err = new HashMap<>();
            err.put("status", "error");
            err.put("message", "Invalid token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(err);
        }

        try {
            MonthlyReportDto response = transactionService.monthlyTransaction(
                    userInfo.getUserId(), month, year, type, status, minAmount);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "FAILED");
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

}
