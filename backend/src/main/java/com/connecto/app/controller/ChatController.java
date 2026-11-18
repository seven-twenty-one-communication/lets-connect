package com.connecto.app.controller;

import com.connecto.app.model.entity.ChatMessage;
import com.connecto.app.model.entity.User;
import com.connecto.app.repository.ChatMessageRepository;
import com.connecto.app.repository.UserBlockRepository;
import com.connecto.app.repository.UserRepository;
import com.connecto.app.service.AuthenticatedUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.HashMap;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final UserBlockRepository userBlockRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final AuthenticatedUser authenticatedUser;

    @GetMapping("/messages")
    public ResponseEntity<List<Map<String, Object>>> getConversation(
            @RequestParam UUID userA,
            @RequestParam UUID userB
    ) {
        authenticatedUser.requireParticipant(userA, userB);
        if (userBlockRepository.existsBlockBetween(userA, userB)) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(chatMessageRepository.findConversationBetween(userA, userB).stream()
                .map(this::messageResponse)
                .toList());
    }

    @PostMapping("/send")
    public ResponseEntity<?> sendMessage(@Valid @RequestBody SendChatMessageRequest request) {
        authenticatedUser.require(request.getSenderId());
        User sender = userRepository.findById(request.getSenderId())
                .orElseThrow(() -> new IllegalArgumentException("Sender not found"));
        User recipient = userRepository.findById(request.getRecipientId())
                .orElseThrow(() -> new IllegalArgumentException("Recipient not found"));
        if (userBlockRepository.existsBlockBetween(sender.getId(), recipient.getId())) {
            return ResponseEntity.status(403).body(Map.of("message", "Messaging is not allowed"));
        }

        ChatMessage message = ChatMessage.builder()
                .sender(sender)
                .recipient(recipient)
                .messageText(request.getMessageText())
                .isRead(false)
                .build();

        chatMessageRepository.save(message);

        // Real-time dispatch via WebSocket
        messagingTemplate.convertAndSend("/topic/chat/" + recipient.getId(), Map.of(
                "type", "NEW_CHAT_MESSAGE",
                "messageId", message.getId(),
                "senderId", sender.getId(),
                "senderNickname", sender.getNickname(),
                "senderAvatar", sender.getAvatarUrl(),
                "messageText", message.getMessageText(),
                "timestamp", System.currentTimeMillis()
        ));

        return ResponseEntity.ok(messageResponse(message));
    }

    private Map<String, Object> messageResponse(ChatMessage message) {
        Map<String, Object> response = new HashMap<>();
        response.put("id", message.getId());
        response.put("senderId", message.getSender().getId());
        response.put("recipientId", message.getRecipient().getId());
        response.put("messageText", message.getMessageText());
        response.put("isRead", message.getIsRead());
        response.put("createdAt", message.getCreatedAt());
        return response;
    }

    @Data
    public static class SendChatMessageRequest {
        @NotNull
        private UUID senderId;
        @NotNull
        private UUID recipientId;
        @NotBlank
        @Size(max = 2000)
        private String messageText;
    }
}
