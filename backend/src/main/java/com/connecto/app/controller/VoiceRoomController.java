package com.connecto.app.controller;

import com.connecto.app.model.entity.Category;
import com.connecto.app.model.entity.User;
import com.connecto.app.model.entity.VoiceRoom;
import com.connecto.app.model.entity.VoiceRoomParticipant;
import com.connecto.app.repository.CategoryRepository;
import com.connecto.app.repository.UserBlockRepository;
import com.connecto.app.repository.UserRepository;
import com.connecto.app.repository.VoiceRoomParticipantRepository;
import com.connecto.app.repository.VoiceRoomRepository;
import com.connecto.app.service.AgoraAccessToken2Service;
import com.connecto.app.service.AuthenticatedUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class VoiceRoomController {
    private final VoiceRoomRepository roomRepository;
    private final VoiceRoomParticipantRepository participantRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final UserBlockRepository userBlockRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final AuthenticatedUser authenticatedUser;
    private final AgoraAccessToken2Service agoraTokenService;

    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<List<Map<String, Object>>> getRooms() {
        return ResponseEntity.ok(roomRepository.findByActiveTrueOrderByCreatedAtDesc().stream()
                .map(this::roomResponse)
                .toList());
    }

    @PostMapping
    @Transactional
    public ResponseEntity<?> createRoom(@Valid @RequestBody CreateRoomRequest request) {
        authenticatedUser.require(request.getHostId());
        User host = userRepository.findById(request.getHostId()).orElse(null);
        if (host == null) {
            return ResponseEntity.notFound().build();
        }

        Category category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId()).orElse(null);
            if (category == null || !Boolean.TRUE.equals(category.getIsActive())) {
                return ResponseEntity.badRequest().body(Map.of("message", "Category not found or inactive"));
            }
        }

        VoiceRoom room = roomRepository.save(VoiceRoom.builder()
                .title(request.getTitle().trim())
                .channelName("room_" + UUID.randomUUID().toString().replace("-", ""))
                .description(request.getDescription())
                .language(request.getLanguage().trim())
                .host(host)
                .category(category)
                .maxParticipants(request.getMaxParticipants())
                .build());
        participantRepository.save(VoiceRoomParticipant.builder().room(room).user(host).build());
        return ResponseEntity.ok(roomResponse(room));
    }

    @PostMapping("/{roomId}/join")
    @Transactional
    public ResponseEntity<?> joinRoom(@PathVariable UUID roomId, @Valid @RequestBody RoomUserRequest request) {
        authenticatedUser.require(request.getUserId());
        VoiceRoom room = roomRepository.findByIdForUpdate(roomId).orElse(null);
        if (room == null || !Boolean.TRUE.equals(room.getActive())) {
            return ResponseEntity.notFound().build();
        }
        User user = userRepository.findById(request.getUserId()).orElse(null);
        if (user == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "User not found"));
        }

        List<VoiceRoomParticipant> activeParticipants =
                participantRepository.findByRoomIdAndActiveTrueOrderByJoinedAtAsc(roomId);
        for (VoiceRoomParticipant participant : activeParticipants) {
            if (userBlockRepository.existsBlockBetween(user.getId(), participant.getUser().getId())) {
                return ResponseEntity.status(403).body(Map.of("message", "Room contains a user you blocked or who blocked you"));
            }
        }

        VoiceRoomParticipant participant = participantRepository.findByRoomIdAndUserId(roomId, user.getId())
                .orElse(null);
        if (participant != null && Boolean.TRUE.equals(participant.getActive())) {
            Map<String, Object> response = roomResponse(room);
            response.put("agoraAppId", agoraTokenService.getAppId());
            response.put("agoraToken", agoraTokenService.buildToken(room.getChannelName(), 0));
            return ResponseEntity.ok(response);
        }
        if (activeParticipants.size() >= room.getMaxParticipants()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Room is full"));
        }

        if (participant == null) {
            participant = VoiceRoomParticipant.builder().room(room).user(user).active(true).build();
        } else {
            participant.setActive(true);
        }
        participantRepository.save(participant);
        messagingTemplate.convertAndSend("/topic/rooms/" + roomId, Map.of(
                "type", "PARTICIPANT_JOINED",
                "userId", user.getId(),
                "nickname", user.getNickname(),
                "activeParticipantCount", activeParticipants.size() + 1
        ));
        Map<String, Object> response = roomResponse(room);
        response.put("agoraAppId", agoraTokenService.getAppId());
        response.put("agoraToken", agoraTokenService.buildToken(room.getChannelName(), 0));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{roomId}/leave")
    @Transactional
    public ResponseEntity<?> leaveRoom(@PathVariable UUID roomId, @Valid @RequestBody RoomUserRequest request) {
        authenticatedUser.require(request.getUserId());
        VoiceRoom room = roomRepository.findByIdForUpdate(roomId).orElse(null);
        if (room == null) {
            return ResponseEntity.notFound().build();
        }
        VoiceRoomParticipant participant = participantRepository.findByRoomIdAndUserId(roomId, request.getUserId())
                .orElse(null);
        if (participant == null || !Boolean.TRUE.equals(participant.getActive())) {
            return ResponseEntity.badRequest().body(Map.of("message", "User is not in this room"));
        }

        participant.setActive(false);
        participantRepository.save(participant);
        if (room.getHost().getId().equals(request.getUserId())) {
            room.setActive(false);
            roomRepository.save(room);
            participantRepository.findByRoomIdAndActiveTrueOrderByJoinedAtAsc(roomId)
                    .forEach(active -> {
                        active.setActive(false);
                        participantRepository.save(active);
                    });
        }
        messagingTemplate.convertAndSend("/topic/rooms/" + roomId, Map.of(
                "type", "PARTICIPANT_LEFT",
                "userId", request.getUserId(),
                "activeParticipantCount", participantRepository.countByRoomIdAndActiveTrue(roomId)
        ));
        return ResponseEntity.ok(Map.of("status", "LEFT"));
    }

    @GetMapping("/{roomId}/participants")
    @Transactional(readOnly = true)
    public ResponseEntity<?> getParticipants(@PathVariable UUID roomId) {
        if (!roomRepository.existsById(roomId)) {
            return ResponseEntity.notFound().build();
        }
        List<Map<String, Object>> participants = participantRepository
                .findByRoomIdAndActiveTrueOrderByJoinedAtAsc(roomId).stream()
                .map(participant -> Map.<String, Object>of(
                        "userId", participant.getUser().getId(),
                        "nickname", participant.getUser().getNickname(),
                        "avatarUrl", participant.getUser().getAvatarUrl(),
                        "joinedAt", participant.getJoinedAt()
                ))
                .toList();
        return ResponseEntity.ok(participants);
    }

    private Map<String, Object> roomResponse(VoiceRoom room) {
        Map<String, Object> response = new HashMap<>();
        response.put("id", room.getId());
        response.put("title", room.getTitle());
        response.put("channelName", room.getChannelName());
        response.put("description", room.getDescription());
        response.put("language", room.getLanguage());
        response.put("hostId", room.getHost().getId());
        response.put("hostNickname", room.getHost().getNickname());
        response.put("categoryId", room.getCategory() == null ? null : room.getCategory().getId());
        response.put("maxParticipants", room.getMaxParticipants());
        response.put("activeParticipantCount", participantRepository.countByRoomIdAndActiveTrue(room.getId()));
        response.put("createdAt", room.getCreatedAt());
        return response;
    }

    @Data
    public static class CreateRoomRequest {
        @NotNull
        private UUID hostId;
        @NotBlank
        @Size(max = 100)
        private String title;
        @Size(max = 500)
        private String description;
        @NotBlank
        @Size(max = 30)
        private String language;
        private Integer categoryId;
        @Min(2)
        @Max(12)
        private int maxParticipants = 8;
    }

    @Data
    public static class RoomUserRequest {
        @NotNull
        private UUID userId;
    }
}
