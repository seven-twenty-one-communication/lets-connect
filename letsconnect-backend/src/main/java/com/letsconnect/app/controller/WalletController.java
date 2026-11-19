package com.letsconnect.app.controller;

import com.letsconnect.app.model.entity.CoinTransaction;
import com.letsconnect.app.repository.CoinPackageRepository;
import com.letsconnect.app.repository.CoinTransactionRepository;
import com.letsconnect.app.service.GooglePlayBillingService;
import com.letsconnect.app.service.AuthenticatedUser;
import com.letsconnect.app.service.WalletService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;
    private final GooglePlayBillingService playBillingService;
    private final CoinTransactionRepository coinTransactionRepository;
    private final CoinPackageRepository coinPackageRepository;
    private final AuthenticatedUser authenticatedUser;

    @GetMapping("/{userId}/balance")
    public ResponseEntity<Map<String, Object>> getBalance(@PathVariable UUID userId) {
        authenticatedUser.require(userId);
        Long balance = walletService.getBalance(userId);
        return ResponseEntity.ok(Map.of(
                "userId", userId,
                "coinBalance", balance
        ));
    }

    @GetMapping("/packages")
    public ResponseEntity<List<Map<String, Object>>> getCoinPackages() {
        return ResponseEntity.ok(coinPackageRepository.findByIsActiveTrueOrderByCoinAmountAsc().stream()
                .map(pack -> Map.<String, Object>of(
                        "id", pack.getId(),
                        "productId", pack.getPlayProductId(),
                        "coinAmount", pack.getCoinAmount(),
                        "bonusCoins", pack.getBonusCoins(),
                        "priceInInr", pack.getPriceInInr(),
                        "title", pack.getDisplayTitle()
                ))
                .toList());
    }

    @GetMapping("/{userId}/transactions")
    public ResponseEntity<List<Map<String, Object>>> getTransactions(@PathVariable UUID userId) {
        authenticatedUser.require(userId);
        List<Map<String, Object>> transactions = coinTransactionRepository
                .findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::transactionResponse)
                .toList();
        return ResponseEntity.ok(transactions);
    }

    /**
     * Production Google Play In-App Purchase Verification Endpoint.
     */
    @PostMapping("/verify-play-purchase")
    public ResponseEntity<?> verifyPlayPurchase(@Valid @RequestBody PlayPurchaseVerificationRequest request) {
        authenticatedUser.require(request.getUserId());
        boolean success = playBillingService.verifyAndCreditPurchase(
                request.getUserId(),
                request.getSignedData(),
                request.getSignature(),
                request.getProductId(),
                request.getOrderId()
        );

        if (success) {
            return ResponseEntity.ok(Map.of(
                    "status", "SUCCESS",
                    "message", "Coins credited successfully",
                    "newBalance", walletService.getBalance(request.getUserId())
            ));
        } else {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "FAILED",
                    "message", "Invalid or duplicate Google Play purchase receipt"
            ));
        }
    }

    private Map<String, Object> transactionResponse(CoinTransaction transaction) {
        return Map.of(
                "id", transaction.getId(),
                "amount", transaction.getAmount(),
                "balanceAfter", transaction.getBalanceAfter(),
                "type", transaction.getTransactionType(),
                "referenceId", transaction.getReferenceId() == null ? "" : transaction.getReferenceId(),
                "description", transaction.getDescription() == null ? "" : transaction.getDescription(),
                "createdAt", transaction.getCreatedAt()
        );
    }

    @Data
    public static class PlayPurchaseVerificationRequest {
        @NotNull
        private UUID userId;
        @NotBlank
        private String signedData;
        @NotBlank
        private String signature;
        @NotBlank
        private String productId;
        @NotBlank
        private String orderId;
    }
}
