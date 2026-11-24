package com.letsconnect.app.controller;

import com.letsconnect.app.model.entity.User;
import com.letsconnect.app.repository.UserRepository;
import com.letsconnect.app.repository.AppSessionRepository;
import com.letsconnect.app.service.AuthenticatedUser;
import com.letsconnect.app.model.enums.UserStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping({"/api/users", "/api/v1", "/api/v1/profiles"})
@RequiredArgsConstructor
public class UserProfileController {
    private final UserRepository userRepository;
    private final AuthenticatedUser authenticatedUser;
    private final AppSessionRepository appSessionRepository;

    @GetMapping("/me")
    @Transactional(readOnly = true)
    public ResponseEntity<?> getMe() {
        return getProfile(authenticatedUser.currentUserId());
    }

    @PatchMapping("/me")
    @Transactional
    public ResponseEntity<?> updateMe(@Valid @RequestBody UpdateProfileRequest request) {
        return updateProfile(authenticatedUser.currentUserId(), request);
    }

    @DeleteMapping("/me")
    @Transactional
    public ResponseEntity<?> deleteMe() {
        UUID userId = authenticatedUser.currentUserId();
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        user.setStatus(UserStatus.SUSPENDED);
        userRepository.save(user);
        appSessionRepository.deleteByUser(user);
        return ResponseEntity.ok(Map.of("status", "DELETION_REQUESTED"));
    }

    @GetMapping("/{userId}")
    @Transactional(readOnly = true)
    public ResponseEntity<?> getProfile(@PathVariable UUID userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        Map<String, Object> response = new HashMap<>();
        response.put("userId", user.getId());
        response.put("nickname", user.getNickname());
        response.put("avatarUrl", user.getAvatarUrl());
        response.put("gender", user.getGender());
        response.put("ageGroup", user.getAgeGroup());
        response.put("preferredLanguages", user.getPreferredLanguages());
        response.put("role", user.getRole());
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{userId}")
    @Transactional
    public ResponseEntity<?> updateProfile(
            @PathVariable UUID userId,
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        authenticatedUser.require(userId);
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        if (request.getNickname() != null) {
            user.setNickname(request.getNickname().trim());
        }
        if (request.getAvatarUrl() != null) {
            user.setAvatarUrl(request.getAvatarUrl().trim());
        }
        if (request.getGender() != null) {
            user.setGender(request.getGender().trim());
        }
        if (request.getAgeGroup() != null) {
            user.setAgeGroup(request.getAgeGroup().trim());
        }
        if (request.getPreferredLanguages() != null) {
            user.setPreferredLanguages(request.getPreferredLanguages().stream()
                    .map(String::trim)
                    .filter(language -> !language.isEmpty())
                    .distinct()
                    .toList());
        }
        userRepository.save(user);
        return getProfile(userId);
    }

    @Data
    public static class UpdateProfileRequest {
        @Size(min = 2, max = 50)
        private String nickname;
        @Size(max = 2000)
        private String avatarUrl;
        @Size(max = 20)
        private String gender;
        @Size(max = 20)
        private String ageGroup;
        @Size(max = 10)
        private List<@Size(max = 30) String> preferredLanguages;
    }
}
