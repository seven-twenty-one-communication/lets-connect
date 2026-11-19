package com.letsconnect.app.controller;

import com.letsconnect.app.dto.CallInitiateRequest;
import com.letsconnect.app.dto.CallTokenResponse;
import com.letsconnect.app.model.entity.CallSession;
import com.letsconnect.app.model.entity.Category;
import com.letsconnect.app.model.entity.ListenerProfile;
import com.letsconnect.app.model.entity.User;
import com.letsconnect.app.model.enums.CallStatus;
import com.letsconnect.app.model.enums.DisconnectReason;
import com.letsconnect.app.repository.CallSessionRepository;
import com.letsconnect.app.repository.CategoryRepository;
import com.letsconnect.app.repository.ListenerProfileRepository;
import com.letsconnect.app.repository.UserBlockRepository;
import com.letsconnect.app.repository.UserRepository;
import com.letsconnect.app.service.AgoraAccessToken2Service;
import com.letsconnect.app.service.AuthenticatedUser;
import com.letsconnect.app.service.CallSignalingService;
import com.letsconnect.app.service.CallBillingProcessor;
import com.letsconnect.app.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.ZonedDateTime;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/calls")
@RequiredArgsConstructor
public class CallController {

    private final UserRepository userRepository;
    private final ListenerProfileRepository listenerProfileRepository;
    private final UserBlockRepository userBlockRepository;
    private final CategoryRepository categoryRepository;
    private final CallSessionRepository callSessionRepository;
    private final AgoraAccessToken2Service agoraTokenService;
    private final CallSignalingService signalingService;
    private final CallBillingProcessor billingProcessor;
    private final WalletService walletService;
    private final AuthenticatedUser authenticatedUser;

    @PostMapping("/initiate")
    @Transactional
    public ResponseEntity<?> initiateCall(@Valid @RequestBody CallInitiateRequest request) {
        authenticatedUser.require(request.getCallerId());
        User caller = userRepository.findById(request.getCallerId())
                .orElseThrow(() -> new IllegalArgumentException("Caller not found"));

        ListenerProfile listenerProfile = listenerProfileRepository.findByUserIdForUpdate(request.getListenerId())
                .orElseThrow(() -> new IllegalArgumentException("Listener profile not found"));

        if (userBlockRepository.existsBlockBetween(caller.getId(), listenerProfile.getUser().getId())) {
            return ResponseEntity.status(403).body("This call is not allowed");
        }
        if (listenerProfile.getKycStatus() != com.letsconnect.app.model.enums.KycStatus.VERIFIED) {
            return ResponseEntity.badRequest().body("Listener is not verified");
        }
        if (!listenerProfile.getIsOnline()) {
            return ResponseEntity.badRequest().body("Listener is currently offline");
        }
        if (listenerProfile.getIsOnCall()) {
            return ResponseEntity.badRequest().body("Listener is currently busy on another call");
        }

        // Enforce minimum balance
        int ratePerMin = listenerProfile.getPricePerMinuteCoins();
        if (ratePerMin <= 0) {
            return ResponseEntity.badRequest().body("Listener call rate is invalid");
        }
        Long callerBalance = walletService.getBalance(caller.getId());
        if (callerBalance < ratePerMin) {
            return ResponseEntity.badRequest().body("Insufficient coin balance. Minimum " + ratePerMin + " coins needed.");
        }

        Category category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId()).orElse(null);
        }

        String channelName = "call_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);

        CallSession session = CallSession.builder()
                .channelName(channelName)
                .caller(caller)
                .listener(listenerProfile.getUser())
                .category(category)
                .ratePerMinuteCoins(ratePerMin)
                .status(CallStatus.RINGING)
                .ringingAt(ZonedDateTime.now())
                .build();

        callSessionRepository.save(session);

        // Mark listener as busy
        listenerProfile.setIsOnCall(true);
        listenerProfileRepository.save(listenerProfile);

        // Generate production Agora AccessToken2 for caller
        String token = agoraTokenService.buildToken(channelName, 0);

        // Dispatch real-time WebSocket incoming call event to listener
        signalingService.sendIncomingCall(
                listenerProfile.getUser().getId(),
                session,
                caller.getNickname(),
                caller.getAvatarUrl(),
                category != null ? category.getName() : "General Venting"
        );

        CallTokenResponse response = CallTokenResponse.builder()
                .callSessionId(session.getId())
                .channelName(channelName)
                .agoraAppId(agoraTokenService.getAppId())
                .agoraToken(token)
                .ratePerMinuteCoins(ratePerMin)
                .callerBalance(callerBalance)
                .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{callId}/accept")
    @Transactional
    public ResponseEntity<?> acceptCall(@PathVariable UUID callId) {
        CallSession session = callSessionRepository.findByIdForUpdate(callId)
                .orElseThrow(() -> new IllegalArgumentException("Call session not found"));
        if (session.getStatus() != CallStatus.RINGING) {
            return ResponseEntity.badRequest().body("Call is no longer waiting for acceptance");
        }
        authenticatedUser.require(session.getListener().getId());

        session.setStatus(CallStatus.CONNECTED);
        session.setConnectedAt(ZonedDateTime.now());
        callSessionRepository.save(session);
        if (!billingProcessor.startCall(session.getId())) {
            session.setStatus(CallStatus.COMPLETED);
            session.setEndedAt(ZonedDateTime.now());
            session.setDisconnectReason(DisconnectReason.BALANCE_EXHAUSTED);
            listenerProfileRepository.findByUserId(session.getListener().getId()).ifPresent(profile -> {
                profile.setIsOnCall(false);
                listenerProfileRepository.save(profile);
            });
            callSessionRepository.save(session);
            signalingService.sendCallEnded(session.getId(), "BALANCE_EXHAUSTED", 0);
            return ResponseEntity.badRequest().body("Insufficient coin balance to start the call");
        }

        // Generate production Agora AccessToken2 for listener
        String listenerToken = agoraTokenService.buildToken(session.getChannelName(), 0);

        // Signal caller that call is answered
        signalingService.sendCallAccepted(session.getId(), listenerToken, session.getChannelName());

        return ResponseEntity.ok(CallTokenResponse.builder()
                .callSessionId(session.getId())
                .channelName(session.getChannelName())
                .agoraAppId(agoraTokenService.getAppId())
                .agoraToken(listenerToken)
                .ratePerMinuteCoins(session.getRatePerMinuteCoins())
                .build());
    }

    @PostMapping("/{callId}/reject")
    @Transactional
    public ResponseEntity<?> rejectCall(
            @PathVariable UUID callId,
            @RequestParam(defaultValue = "Listener is currently unavailable") String reason
    ) {
        CallSession session = callSessionRepository.findByIdForUpdate(callId)
                .orElseThrow(() -> new IllegalArgumentException("Call session not found"));
        if (session.getStatus() != CallStatus.RINGING) {
            return ResponseEntity.badRequest().body("Only a ringing call can be rejected");
        }
        authenticatedUser.require(session.getListener().getId());

        session.setStatus(CallStatus.REJECTED);
        session.setEndedAt(ZonedDateTime.now());
        session.setDisconnectReason(DisconnectReason.LISTENER_HANGUP);
        callSessionRepository.save(session);

        // Free the listener
        listenerProfileRepository.findByUserId(session.getListener().getId()).ifPresent(profile -> {
            profile.setIsOnCall(false);
            listenerProfileRepository.save(profile);
        });

        signalingService.sendCallRejected(session.getId(), reason);
        return ResponseEntity.ok("Call rejected");
    }

    @PostMapping("/{callId}/end")
    @Transactional
    public ResponseEntity<?> endCall(
            @PathVariable UUID callId,
            @RequestParam(defaultValue = "CALLER_HANGUP") DisconnectReason reason
    ) {
        CallSession session = callSessionRepository.findByIdForUpdate(callId)
                .orElseThrow(() -> new IllegalArgumentException("Call session not found"));
        if (session.getStatus() != CallStatus.RINGING && session.getStatus() != CallStatus.CONNECTED) {
            return ResponseEntity.badRequest().body("Call has already ended");
        }
        authenticatedUser.requireParticipant(session.getCaller().getId(), session.getListener().getId());

        session.setStatus(CallStatus.COMPLETED);
        session.setEndedAt(ZonedDateTime.now());
        if (session.getConnectedAt() != null) {
            session.setDurationSeconds(Math.toIntExact(Math.max(
                    0,
                    java.time.Duration.between(session.getConnectedAt(), session.getEndedAt()).getSeconds()
            )));
        }
        session.setDisconnectReason(reason);
        callSessionRepository.save(session);

        // Free the listener
        listenerProfileRepository.findByUserId(session.getListener().getId()).ifPresent(profile -> {
            profile.setIsOnCall(false);
            listenerProfileRepository.save(profile);
        });

        signalingService.sendCallEnded(session.getId(), reason.name(), session.getDurationSeconds());
        return ResponseEntity.ok("Call ended successfully");
    }
}
