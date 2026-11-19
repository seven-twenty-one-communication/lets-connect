package com.letsconnect.app.service;

import com.letsconnect.app.model.entity.CallSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CallSignalingService {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Wakes up the listener's phone via WebSocket (and companion FCM) with full incoming call UI data.
     */
    public void sendIncomingCall(
            UUID listenerUserId,
            CallSession session,
            String callerNickname,
            String callerAvatar,
            String categoryName
    ) {
        String destination = "/topic/listener/" + listenerUserId;
        Map<String, Object> payload = Map.of(
                "type", "INCOMING_CALL",
                "callSessionId", session.getId(),
                "channelName", session.getChannelName(),
                "callerNickname", callerNickname,
                "callerAvatar", callerAvatar,
                "categoryName", categoryName != null ? categoryName : "General Venting",
                "ratePerMinuteCoins", session.getRatePerMinuteCoins(),
                "timestamp", System.currentTimeMillis()
        );

        log.info("Dispatching INCOMING_CALL to listener {}", listenerUserId);
        messagingTemplate.convertAndSend(destination, payload);
    }

    /**
     * Notifies caller that listener accepted the call.
     */
    public void sendCallAccepted(UUID callSessionId, String agoraToken, String channelName) {
        String destination = "/topic/call/" + callSessionId;
        Map<String, Object> payload = Map.of(
                "type", "CALL_ACCEPTED",
                "callSessionId", callSessionId,
                "agoraToken", agoraToken,
                "channelName", channelName,
                "timestamp", System.currentTimeMillis()
        );

        log.info("Dispatching CALL_ACCEPTED for call {}", callSessionId);
        messagingTemplate.convertAndSend(destination, payload);
    }

    /**
     * Notifies caller that listener is busy or declined.
     */
    public void sendCallRejected(UUID callSessionId, String reason) {
        String destination = "/topic/call/" + callSessionId;
        Map<String, Object> payload = Map.of(
                "type", "CALL_REJECTED",
                "callSessionId", callSessionId,
                "reason", reason,
                "timestamp", System.currentTimeMillis()
        );

        log.info("Dispatching CALL_REJECTED for call {}: {}", callSessionId, reason);
        messagingTemplate.convertAndSend(destination, payload);
    }

    /**
     * Warns both participants that caller's coins are about to expire (1 minute left).
     */
    public void sendLowBalanceWarning(UUID callSessionId, long remainingCoins, int minutesLeft) {
        String destination = "/topic/call/" + callSessionId;
        Map<String, Object> payload = Map.of(
                "type", "LOW_BALANCE_WARNING",
                "callSessionId", callSessionId,
                "remainingCoins", remainingCoins,
                "minutesLeft", minutesLeft,
                "timestamp", System.currentTimeMillis()
        );

        log.info("Dispatching LOW_BALANCE_WARNING for call {}", callSessionId);
        messagingTemplate.convertAndSend(destination, payload);
    }

    /**
     * Notifies both participants that the call is terminated.
     */
    public void sendCallEnded(UUID callSessionId, String disconnectReason, int durationSeconds) {
        String destination = "/topic/call/" + callSessionId;
        Map<String, Object> payload = Map.of(
                "type", "CALL_ENDED",
                "callSessionId", callSessionId,
                "disconnectReason", disconnectReason,
                "durationSeconds", durationSeconds,
                "timestamp", System.currentTimeMillis()
        );

        log.info("Dispatching CALL_ENDED for call {}: {}", callSessionId, disconnectReason);
        messagingTemplate.convertAndSend(destination, payload);
    }
}
