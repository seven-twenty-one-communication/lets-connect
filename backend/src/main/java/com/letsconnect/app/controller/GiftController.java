package com.letsconnect.app.controller;

import com.letsconnect.app.model.entity.CallSession;
import com.letsconnect.app.model.entity.Gift;
import com.letsconnect.app.model.entity.GiftTransaction;
import com.letsconnect.app.model.entity.User;
import com.letsconnect.app.model.enums.TransactionType;
import com.letsconnect.app.repository.CallSessionRepository;
import com.letsconnect.app.repository.GiftRepository;
import com.letsconnect.app.repository.GiftTransactionRepository;
import com.letsconnect.app.repository.UserBlockRepository;
import com.letsconnect.app.repository.UserRepository;
import com.letsconnect.app.service.WalletService;
import com.letsconnect.app.service.AuthenticatedUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/gifts")
@RequiredArgsConstructor
public class GiftController {

    private final GiftRepository giftRepository;
    private final GiftTransactionRepository giftTransactionRepository;
    private final UserRepository userRepository;
    private final UserBlockRepository userBlockRepository;
    private final CallSessionRepository callSessionRepository;
    private final WalletService walletService;
    private final SimpMessagingTemplate messagingTemplate;
    private final AuthenticatedUser authenticatedUser;

    @GetMapping
    public ResponseEntity<List<Gift>> getActiveGifts() {
        return ResponseEntity.ok(giftRepository.findByIsActiveTrueOrderByCoinPriceAsc());
    }

    @PostMapping("/send")
    @Transactional
    public ResponseEntity<?> sendGift(@Valid @RequestBody SendGiftRequest request) {
        authenticatedUser.require(request.getSenderId());
        User sender = userRepository.findById(request.getSenderId())
                .orElseThrow(() -> new IllegalArgumentException("Sender not found"));
        User recipient = userRepository.findById(request.getRecipientId())
                .orElseThrow(() -> new IllegalArgumentException("Recipient not found"));
        if (userBlockRepository.existsBlockBetween(sender.getId(), recipient.getId())) {
            return ResponseEntity.status(403).body(Map.of("message", "Gift sending is not allowed"));
        }
        Gift gift = giftRepository.findById(request.getGiftId())
                .orElseThrow(() -> new IllegalArgumentException("Gift not found"));
        if (gift.getCoinPrice() <= 0) {
            return ResponseEntity.badRequest().body(Map.of("message", "Gift price is invalid"));
        }

        CallSession session = null;
        if (request.getCallSessionId() != null) {
            session = callSessionRepository.findById(request.getCallSessionId()).orElse(null);
            if (session == null
                    || (session.getStatus() != com.letsconnect.app.model.enums.CallStatus.CONNECTED)
                    || !(session.getCaller().getId().equals(sender.getId())
                    && session.getListener().getId().equals(recipient.getId()))
                    && !(session.getListener().getId().equals(sender.getId())
                    && session.getCaller().getId().equals(recipient.getId()))) {
                return ResponseEntity.badRequest().body(Map.of("message", "Gift call session is invalid"));
            }
        }

        // 1. Deduct coins from sender
        boolean debited = walletService.debitCoins(
                sender.getId(),
                gift.getCoinPrice(),
                TransactionType.CALL_DEBIT,
                "GIFT_" + gift.getId(),
                "Sent virtual gift: " + gift.getName()
        );

        if (!debited) {
            return ResponseEntity.badRequest().body("Insufficient coin balance to send this gift");
        }

        // 2. Credit listener (e.g. 70% of gift value)
        int recipientCoins = (int) Math.round(gift.getCoinPrice() * 0.70);
        walletService.creditListenerEarnings(
                recipient.getId(),
                recipientCoins,
                "GIFT_" + gift.getId(),
                "Received virtual gift: " + gift.getName() + " from " + sender.getNickname()
        );

        // 3. Save transaction record
        GiftTransaction transaction = GiftTransaction.builder()
                .callSession(session)
                .sender(sender)
                .recipient(recipient)
                .gift(gift)
                .coinsSpent(gift.getCoinPrice())
                .recipientCoinsEarned(recipientCoins)
                .build();
        giftTransactionRepository.save(transaction);

        // 4. Dispatch in-call animation to active call channel
        if (session != null) {
            messagingTemplate.convertAndSend("/topic/call/" + session.getId(), Map.of(
                    "type", "GIFT_SENT",
                    "giftName", gift.getName(),
                    "emoji", gift.getEmoji(),
                    "animationEffect", gift.getAnimationEffect(),
                    "senderNickname", sender.getNickname(),
                    "timestamp", System.currentTimeMillis()
            ));
        }

        return ResponseEntity.ok(Map.of(
                "status", "SUCCESS",
                "gift", gift.getName(),
                "coinsSpent", gift.getCoinPrice(),
                "senderNewBalance", walletService.getBalance(sender.getId())
        ));
    }

    @Data
    public static class SendGiftRequest {
        @NotNull
        private UUID senderId;
        @NotNull
        private UUID recipientId;
        @NotNull
        private Integer giftId;
        @NotNull
        private UUID callSessionId;
    }
}
