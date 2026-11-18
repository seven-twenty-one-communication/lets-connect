package com.connecto.app.service;

import com.connecto.app.model.entity.CallSession;
import com.connecto.app.model.entity.ListenerProfile;
import com.connecto.app.model.enums.CallStatus;
import com.connecto.app.model.enums.DisconnectReason;
import com.connecto.app.model.enums.TransactionType;
import com.connecto.app.repository.CallSessionRepository;
import com.connecto.app.repository.ListenerProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZonedDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CallBillingProcessor {
    private final CallSessionRepository callSessionRepository;
    private final ListenerProfileRepository listenerProfileRepository;
    private final WalletService walletService;
    private final CallSignalingService signalingService;

    @Transactional
    public boolean startCall(UUID callId) {
        CallSession call = callSessionRepository.findByIdForUpdate(callId).orElse(null);
        if (call == null || call.getStatus() != CallStatus.CONNECTED) {
            return false;
        }
        if (call.getLastBilledAt() != null) {
            return true;
        }
        return chargeMinute(call, ZonedDateTime.now());
    }

    @Transactional
    public void processCall(UUID callId) {
        CallSession call = callSessionRepository.findByIdForUpdate(callId).orElse(null);
        if (call == null || call.getStatus() != CallStatus.CONNECTED || call.getConnectedAt() == null) {
            return;
        }

        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime billingReference = call.getLastBilledAt() != null
                ? call.getLastBilledAt()
                : call.getConnectedAt();
        if (billingReference.plusSeconds(60).isAfter(now)) {
            return;
        }

        if (!chargeMinute(call, now)) {
            log.info("Terminating call {} due to exhausted balance for caller {}", call.getId(), call.getCaller().getId());
            call.setStatus(CallStatus.COMPLETED);
            call.setEndedAt(now);
            call.setDurationSeconds(elapsedSeconds(call, now));
            call.setDisconnectReason(DisconnectReason.BALANCE_EXHAUSTED);
            listenerProfileRepository.findByUserId(call.getListener().getId()).ifPresent(profile -> {
                profile.setIsOnCall(false);
                listenerProfileRepository.save(profile);
            });
            signalingService.sendCallEnded(call.getId(), "BALANCE_EXHAUSTED", call.getDurationSeconds());
            return;
        }

    }

    private boolean chargeMinute(CallSession call, ZonedDateTime billedAt) {
        int ratePerMinute = call.getRatePerMinuteCoins();
        String refId = call.getId().toString();
        boolean debited = walletService.debitCoins(
                call.getCaller().getId(),
                ratePerMinute,
                TransactionType.CALL_DEBIT,
                refId,
                "Per-minute call billing for session: " + call.getChannelName()
        );
        if (!debited) {
            return false;
        }

        ListenerProfile listenerProfile = listenerProfileRepository.findByUserId(call.getListener().getId())
                .orElse(null);
        BigDecimal payoutPercentage = listenerProfile != null && listenerProfile.getPayoutPercentage() != null
                ? listenerProfile.getPayoutPercentage()
                : new BigDecimal("60.00");
        long listenerCoins = BigDecimal.valueOf(ratePerMinute)
                .multiply(payoutPercentage)
                .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP)
                .longValue();
        long platformCoins = ratePerMinute - listenerCoins;

        walletService.creditListenerEarnings(
                call.getListener().getId(),
                listenerCoins,
                refId,
                "Earning from call session: " + call.getChannelName()
        );

        call.setLastBilledAt(billedAt);
        call.setTotalCoinsCharged(call.getTotalCoinsCharged() + ratePerMinute);
        call.setListenerCoinsEarned(call.getListenerCoinsEarned() + listenerCoins);
        call.setPlatformCommissionCoins(call.getPlatformCommissionCoins() + platformCoins);
        callSessionRepository.save(call);

        Long currentCallerBalance = walletService.getBalance(call.getCaller().getId());
        if (currentCallerBalance < ratePerMinute) {
            signalingService.sendLowBalanceWarning(call.getId(), currentCallerBalance, 0);
        } else if (currentCallerBalance < ratePerMinute * 2L) {
            signalingService.sendLowBalanceWarning(call.getId(), currentCallerBalance, 1);
        }
        log.debug("Billed call {} - 1 min: {} coins (Listener earned {}, Platform took {})",
                call.getId(), ratePerMinute, listenerCoins, platformCoins);
        return true;
    }

    private int elapsedSeconds(CallSession call, ZonedDateTime endedAt) {
        if (call.getConnectedAt() == null) {
            return 0;
        }
        return Math.toIntExact(Math.max(0, java.time.Duration.between(call.getConnectedAt(), endedAt).getSeconds()));
    }
}
