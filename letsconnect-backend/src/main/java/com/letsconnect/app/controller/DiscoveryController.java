package com.letsconnect.app.controller;

import com.letsconnect.app.model.entity.ListenerProfile;
import com.letsconnect.app.model.enums.KycStatus;
import com.letsconnect.app.repository.CategoryRepository;
import com.letsconnect.app.repository.ListenerProfileRepository;
import com.letsconnect.app.repository.UserBlockRepository;
import com.letsconnect.app.service.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/discover")
@RequiredArgsConstructor
public class DiscoveryController {
    private final ListenerProfileRepository listenerProfileRepository;
    private final CategoryRepository categoryRepository;
    private final UserBlockRepository userBlockRepository;
    private final AuthenticatedUser authenticatedUser;

    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<List<Map<String, Object>>> discover(
            @RequestParam(required = false) String topic,
            @RequestParam(required = false) String language
    ) {
        UUID viewerId = authenticatedUser.currentUserId();
        Integer categoryId = null;
        if (topic != null && !topic.isBlank() && !"all".equalsIgnoreCase(topic)) {
            categoryId = categoryRepository.findBySlug(topic.trim().toLowerCase())
                    .map(category -> Boolean.TRUE.equals(category.getIsActive()) ? category.getId() : null)
                    .orElse(null);
            if (categoryId == null) {
                return ResponseEntity.badRequest().build();
            }
        }

        return ResponseEntity.ok(listenerProfileRepository.findFilteredListeners(
                        KycStatus.VERIFIED,
                        categoryId,
                        true
                ).stream()
                .filter(profile -> !viewerId.equals(profile.getUser().getId()))
                .filter(profile -> !userBlockRepository.existsBlockBetween(
                        viewerId,
                        profile.getUser().getId()
                ))
                .filter(profile -> language == null || language.isBlank()
                        || profile.getUser().getPreferredLanguages().contains(language.trim()))
                .filter(profile -> !Boolean.FALSE.equals(profile.getIsOnline()))
                .map(this::publicProjection)
                .toList());
    }

    private Map<String, Object> publicProjection(ListenerProfile profile) {
        return Map.of(
                "userId", profile.getUser().getId(),
                "nickname", profile.getUser().getNickname(),
                "avatarUrl", profile.getUser().getAvatarUrl(),
                "languages", profile.getUser().getPreferredLanguages(),
                "bio", profile.getBio() == null ? "" : profile.getBio(),
                "pricePerMinuteCoins", profile.getPricePerMinuteCoins(),
                "isOnline", profile.getIsOnline(),
                "isOnCall", profile.getIsOnCall(),
                "ratingAvg", profile.getRatingAvg(),
                "categories", profile.getCategories().stream()
                        .filter(category -> Boolean.TRUE.equals(category.getIsActive()))
                        .map(category -> Map.of(
                                "id", category.getId(),
                                "name", category.getName(),
                                "slug", category.getSlug()
                        ))
                        .toList()
        );
    }
}
