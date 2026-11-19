package com.letsconnect.app.controller;

import com.letsconnect.app.model.enums.TransactionType;
import com.letsconnect.app.service.WalletService;
import com.letsconnect.app.service.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
@Profile({"dev", "test"})
public class TestWalletController {
    private final WalletService walletService;
    private final AuthenticatedUser authenticatedUser;

    @PostMapping("/{userId}/add-test-coins")
    public ResponseEntity<?> addTestCoins(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "100") long coins
    ) {
        authenticatedUser.require(userId);
        if (coins <= 0) {
            return ResponseEntity.badRequest().body(Map.of("message", "Coin amount must be positive"));
        }
        walletService.creditCoins(
                userId,
                coins,
                TransactionType.ADMIN_CREDIT,
                "TEST_RECHARGE_" + UUID.randomUUID(),
                "Development test recharge"
        );
        return ResponseEntity.ok(Map.of(
                "message", "Successfully added " + coins + " test coins",
                "newBalance", walletService.getBalance(userId)
        ));
    }
}
