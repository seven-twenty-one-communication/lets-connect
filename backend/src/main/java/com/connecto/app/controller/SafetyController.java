package com.connecto.app.controller;

import com.connecto.app.model.entity.User;
import com.connecto.app.model.entity.UserBlock;
import com.connecto.app.model.entity.UserReport;
import com.connecto.app.repository.UserBlockRepository;
import com.connecto.app.repository.UserReportRepository;
import com.connecto.app.repository.UserRepository;
import com.connecto.app.service.AuthenticatedUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/safety")
@RequiredArgsConstructor
public class SafetyController {
    private final UserRepository userRepository;
    private final UserBlockRepository userBlockRepository;
    private final UserReportRepository userReportRepository;
    private final AuthenticatedUser authenticatedUser;

    @PostMapping("/{userId}/blocks")
    @Transactional
    public ResponseEntity<?> blockUser(
            @PathVariable UUID userId,
            @Valid @RequestBody BlockUserRequest request
    ) {
        authenticatedUser.require(userId);
        if (userId.equals(request.getBlockedUserId())) {
            return ResponseEntity.badRequest().body(Map.of("message", "You cannot block yourself"));
        }
        User blocker = userRepository.findById(userId).orElse(null);
        User blocked = userRepository.findById(request.getBlockedUserId()).orElse(null);
        if (blocker == null || blocked == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "User not found"));
        }
        if (userBlockRepository.findByBlockerIdAndBlockedId(userId, blocked.getId()).isEmpty()) {
            userBlockRepository.save(UserBlock.builder().blocker(blocker).blocked(blocked).build());
        }
        return ResponseEntity.ok(Map.of("status", "BLOCKED", "blockedUserId", blocked.getId()));
    }

    @DeleteMapping("/{userId}/blocks/{blockedUserId}")
    @Transactional
    public ResponseEntity<?> unblockUser(
            @PathVariable UUID userId,
            @PathVariable UUID blockedUserId
    ) {
        authenticatedUser.require(userId);
        userBlockRepository.findByBlockerIdAndBlockedId(userId, blockedUserId)
                .ifPresent(userBlockRepository::delete);
        return ResponseEntity.ok(Map.of("status", "UNBLOCKED", "blockedUserId", blockedUserId));
    }

    @GetMapping("/{userId}/blocks")
    @Transactional(readOnly = true)
    public ResponseEntity<List<Map<String, Object>>> getBlockedUsers(@PathVariable UUID userId) {
        authenticatedUser.require(userId);
        return ResponseEntity.ok(userBlockRepository.findByBlockerIdOrderByCreatedAtDesc(userId).stream()
                .map(block -> Map.<String, Object>of(
                        "userId", block.getBlocked().getId(),
                        "nickname", block.getBlocked().getNickname(),
                        "createdAt", block.getCreatedAt()
                ))
                .toList());
    }

    @PostMapping("/{userId}/reports")
    @Transactional
    public ResponseEntity<?> reportUser(
            @PathVariable UUID userId,
            @Valid @RequestBody ReportUserRequest request
    ) {
        authenticatedUser.require(userId);
        if (userId.equals(request.getReportedUserId())) {
            return ResponseEntity.badRequest().body(Map.of("message", "You cannot report yourself"));
        }
        User reporter = userRepository.findById(userId).orElse(null);
        User reported = userRepository.findById(request.getReportedUserId()).orElse(null);
        if (reporter == null || reported == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "User not found"));
        }
        UserReport report = userReportRepository.save(UserReport.builder()
                .reporter(reporter)
                .reportedUser(reported)
                .reason(request.getReason().trim())
                .details(request.getDetails())
                .build());
        return ResponseEntity.ok(Map.of("reportId", report.getId(), "status", report.getStatus()));
    }

    @Data
    public static class BlockUserRequest {
        @NotNull
        private UUID blockedUserId;
    }

    @Data
    public static class ReportUserRequest {
        @NotNull
        private UUID reportedUserId;
        @NotBlank
        @Size(max = 40)
        private String reason;
        @Size(max = 1000)
        private String details;
    }
}
