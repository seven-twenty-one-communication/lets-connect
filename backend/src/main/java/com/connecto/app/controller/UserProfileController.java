package com.connecto.app.controller;

import com.connecto.app.model.entity.User;
import com.connecto.app.repository.UserRepository;
import com.connecto.app.service.AuthenticatedUser;
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
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserProfileController {
    private final UserRepository userRepository;
    private final AuthenticatedUser authenticatedUser;

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
