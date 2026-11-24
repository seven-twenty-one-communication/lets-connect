package com.letsconnect.app.controller;

import com.letsconnect.app.model.entity.ListenerProfile;
import com.letsconnect.app.model.entity.User;
import com.letsconnect.app.model.enums.KycStatus;
import com.letsconnect.app.model.enums.UserRole;
import com.letsconnect.app.repository.CategoryRepository;
import com.letsconnect.app.repository.ListenerProfileRepository;
import com.letsconnect.app.repository.UserRepository;
import com.letsconnect.app.service.AuthenticatedUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping({"/api/listeners", "/api/v1/listeners"})
@RequiredArgsConstructor
public class ListenerController {

    private final ListenerProfileRepository listenerProfileRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final AuthenticatedUser authenticatedUser;

    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<List<Map<String, Object>>> getListeners(
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) Boolean isOnline
    ) {
        List<ListenerProfile> listeners = listenerProfileRepository.findFilteredListeners(
                KycStatus.VERIFIED,
                categoryId,
                isOnline
        );
        return ResponseEntity.ok(listeners.stream().map(this::listenerResponse).toList());
    }

    @PostMapping("/apply")
    @Transactional
    public ResponseEntity<?> applyAsListener(@Valid @RequestBody ListenerApplicationRequest request) {
        authenticatedUser.require(request.getUserId());
        User user = userRepository.findById(request.getUserId()).orElse(null);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        List<com.letsconnect.app.model.entity.Category> categories =
                categoryRepository.findAllById(request.getCategoryIds());
        if (categories.size() != request.getCategoryIds().stream().distinct().count()
                || categories.stream().anyMatch(category -> !Boolean.TRUE.equals(category.getIsActive()))) {
            return ResponseEntity.badRequest().body(Map.of("message", "One or more categories are invalid"));
        }

        ListenerProfile profile = listenerProfileRepository.findByUserId(user.getId()).orElse(null);
        if (profile != null && profile.getKycStatus() == KycStatus.VERIFIED) {
            return ResponseEntity.badRequest().body(Map.of("message", "Verified listener profile cannot be reapplied"));
        }
        if (profile == null) {
            profile = ListenerProfile.builder().user(user).build();
        }
        profile.setBio(request.getBio());
        profile.setVoiceIntroUrl(request.getVoiceIntroUrl());
        profile.setPricePerMinuteCoins(request.getPricePerMinuteCoins());
        profile.setPayoutUpiId(request.getPayoutUpiId());
        profile.getCategories().clear();
        profile.getCategories().addAll(categories);
        profile.setKycStatus(KycStatus.PENDING);
        profile.setIsOnline(false);
        listenerProfileRepository.save(profile);
        user.setRole(UserRole.LISTENER);
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("status", "PENDING", "listenerId", profile.getId()));
    }

    @PostMapping("/{userId}/toggle-online")
    @Transactional
    public ResponseEntity<?> toggleOnline(@PathVariable UUID userId) {
        authenticatedUser.require(userId);
        ListenerProfile profile = listenerProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Listener profile not found for user: " + userId));

        if (profile.getKycStatus() != KycStatus.VERIFIED) {
            return ResponseEntity.status(403).body("Listener account is not verified");
        }
        profile.setIsOnline(!profile.getIsOnline());
        profile.setLastSeenAt(ZonedDateTime.now());
        listenerProfileRepository.save(profile);

        return ResponseEntity.ok(listenerResponse(profile));
    }

    /**
     * Updates the 5-10s Voice Intro Snippet audio URL for the listener.
     */
    @PostMapping("/{userId}/voice-intro")
    @Transactional
    public ResponseEntity<?> updateVoiceIntro(
            @PathVariable UUID userId,
            @RequestBody VoiceIntroRequest request
    ) {
        authenticatedUser.require(userId);
        ListenerProfile profile = listenerProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Listener profile not found for user: " + userId));

        profile.setVoiceIntroUrl(request.getVoiceIntroUrl());
        listenerProfileRepository.save(profile);

        return ResponseEntity.ok(listenerResponse(profile));
    }

    @Data
    public static class VoiceIntroRequest {
        private String voiceIntroUrl;
    }

    @Data
    public static class ListenerApplicationRequest {
        @NotNull
        private UUID userId;
        @Size(max = 2000)
        private String bio;
        @Size(max = 2000)
        private String voiceIntroUrl;
        @Min(1)
        @Max(10000)
        private int pricePerMinuteCoins = 10;
        @Size(max = 100)
        private String payoutUpiId;
        @NotEmpty
        @Size(max = 10)
        private List<Integer> categoryIds;
    }

    private Map<String, Object> listenerResponse(ListenerProfile profile) {
        Map<String, Object> response = new java.util.HashMap<>();
        response.put("listenerId", profile.getId());
        response.put("userId", profile.getUser().getId());
        response.put("nickname", profile.getUser().getNickname());
        response.put("avatarUrl", profile.getUser().getAvatarUrl());
        response.put("languages", profile.getUser().getPreferredLanguages());
        response.put("bio", profile.getBio());
        response.put("voiceIntroUrl", profile.getVoiceIntroUrl());
        response.put("pricePerMinuteCoins", profile.getPricePerMinuteCoins());
        response.put("isOnline", profile.getIsOnline());
        response.put("isOnCall", profile.getIsOnCall());
        response.put("ratingAvg", profile.getRatingAvg());
        response.put("totalReviews", profile.getTotalReviews());
        response.put("totalCallsCompleted", profile.getTotalCallsCompleted());
        response.put("kycStatus", profile.getKycStatus());
        response.put("categories", profile.getCategories().stream()
                .map(category -> Map.of("id", category.getId(), "name", category.getName(), "slug", category.getSlug()))
                .toList());
        return response;
    }
}
