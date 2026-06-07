package com.transaction.wallet.service;

import com.transaction.wallet.model.dto.MonthlyReportDto;
import com.transaction.wallet.model.dto.TranscationDto;
import com.transaction.wallet.model.dto.TransferResponseDto;
import com.transaction.wallet.model.entity.Wallet;
import com.transaction.wallet.model.entity.Transaction;
import com.transaction.wallet.repository.WalletRepository;
import com.transaction.wallet.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class TransactionServiceImpl implements TransactionService {

        private static final Logger log = LoggerFactory.getLogger(TransactionServiceImpl.class);

        private final WalletRepository walletRepository;
        private final TransactionRepository transactionRepository;
        private final NotificationClient notificationClient;

        public TransactionServiceImpl(WalletRepository walletRepository,
                        TransactionRepository transactionRepository,
                        NotificationClient notificationClient) {
                this.walletRepository = walletRepository;
                this.transactionRepository = transactionRepository;
                this.notificationClient = notificationClient;
        }

        @Override
        @Transactional
        public TransferResponseDto transfer(Long senderUserId, TranscationDto request) {
                // 1. Get sender wallet
                Wallet senderWallet = walletRepository.findByUserId(senderUserId)
                                .orElseThrow(() -> new IllegalArgumentException("Sender wallet not found"));

                // 2. Get recipient wallet
                Wallet recipientWallet = walletRepository.findById(request.getRecipientWalletId())
                                .orElseThrow(() -> new IllegalArgumentException("Recipient wallet not found"));

                // 3. Prevent self-transfer
                if (senderWallet.getId().equals(recipientWallet.getId())) {
                        throw new IllegalArgumentException("Cannot transfer to own wallet");
                }

                // 4. Lock sender and recipient wallets in ascending ID order to prevent
                // deadlock
                Long senderId = senderWallet.getId();
                Long recipientId = recipientWallet.getId();

                Long firstId = Math.min(senderId, recipientId);
                Long secondId = Math.max(senderId, recipientId);

                // Fetch locked wallets (performs SELECT FOR UPDATE)
                Wallet lockedFirst = walletRepository.findByIdForUpdate(firstId)
                                .orElseThrow(() -> new IllegalArgumentException("Wallet not found during locking"));
                Wallet lockedSecond = walletRepository.findByIdForUpdate(secondId)
                                .orElseThrow(() -> new IllegalArgumentException("Wallet not found during locking"));

                Wallet lockedSender = lockedFirst.getId().equals(senderId) ? lockedFirst : lockedSecond;
                Wallet lockedRecipient = lockedFirst.getId().equals(recipientId) ? lockedFirst : lockedSecond;

                // 5. Debit & Credit
                lockedSender.debit(request.getAmount());
                lockedRecipient.credit(request.getAmount());

                // 6. Save wallets
                walletRepository.save(lockedSender);
                walletRepository.save(lockedRecipient);

                // 7. Generate Reference Number
                String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
                String randomStr = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
                String refNum = "TX-" + dateStr + "-" + randomStr;

                // 8. Log and Save Transaction
                Transaction tx = new Transaction();
                tx.setWalletId(lockedSender.getId());
                tx.setCounterpartyWalletId(lockedRecipient.getId());
                tx.setAmount(request.getAmount());
                tx.setType("TRANSFER_OUT");
                tx.setStatus("SUCCESS");
                tx.setReferenceNumber(refNum);
                tx.setDescription(request.getDescription());
                tx.setCreatedAt(Instant.now());
                transactionRepository.save(tx);

                // 9. Next update : Send notification asynchronously
                try {
                        Map<String, Object> notificationReq = new HashMap<>();
                        notificationReq.put("userId", lockedRecipient.getUserId());
                        notificationReq.put("type", "EMAIL");
                        notificationReq.put("message", "You have received " + lockedSender.getCurrency() + " "
                                        + String.format("%.2f", request.getAmount()) + " from User "
                                        + lockedSender.getUserId() + ".");
                        notificationClient.sendNotification(notificationReq);
                } catch (Exception e) {
                        log.error("Failed to send transfer notification to recipient: {}", e.getMessage());
                }

                return TransferResponseDto.builder()
                                .status("SUCCESS")
                                .referenceNumber(refNum)
                                .amount(request.getAmount())
                                .senderWalletId(lockedSender.getId())
                                .recipientWalletId(lockedRecipient.getId())
                                .description(request.getDescription())
                                .timestamp(tx.getCreatedAt().toString())
                                .build();
        }

        @Override
        public MonthlyReportDto monthlyTransaction(Long userId, int month, int year, String type, String status,
                        Double minAmount) {
                Wallet wallet = walletRepository.findByUserId(userId)
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "Wallet not found for user ID: " + userId));
                Long walletId = wallet.getId();

                List<Transaction> transactions = transactionRepository.getMonthlyTransactionWithFilters(
                                userId, month, year, type, status, minAmount);

                List<TranscationDto> mappedTx = transactions.stream()
                                .map(tx -> {
                                        String resolvedType = tx.getType();
                                        Long counterpartyId = tx.getCounterpartyWalletId();
                                        if (walletId.equals(tx.getCounterpartyWalletId())) {
                                                resolvedType = "TRANSFER_IN";
                                                counterpartyId = tx.getWalletId();
                                        }
                                        return TranscationDto.builder()
                                                        .id(tx.getId())
                                                        .walletId(walletId)
                                                        .counterpartyWalletId(counterpartyId)
                                                        .amount(tx.getAmount())
                                                        .type(resolvedType)
                                                        .status(tx.getStatus())
                                                        .referenceNumber(tx.getReferenceNumber())
                                                        .description(tx.getDescription())
                                                        .createdAt(tx.getCreatedAt().toString())
                                                        .build();
                                })
                                .toList();

                double totalVolume = mappedTx.stream()
                                .filter(tx -> "SUCCESS".equals(tx.getStatus()))
                                .mapToDouble(TranscationDto::getAmount)
                                .reduce(0.0, Double::sum);

                java.util.Map<String, Double> volumeByType = mappedTx.stream()
                                .filter(tx -> "SUCCESS".equals(tx.getStatus()))
                                .collect(java.util.stream.Collectors.groupingBy(
                                                TranscationDto::getType,
                                                java.util.stream.Collectors.summingDouble(TranscationDto::getAmount)));

                log.info("Monthly transaction report for userId {}: totalVolume={}, volumesByType={}", userId,
                                totalVolume, volumeByType);

                return MonthlyReportDto.builder()
                                .totalVolume(totalVolume)
                                .currency(wallet.getCurrency())
                                .walletId(walletId)
                                .volumeByType(volumeByType)
                                .transactions(mappedTx)
                                .build();
        }
}
