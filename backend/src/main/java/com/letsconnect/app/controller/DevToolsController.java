package com.letsconnect.app.controller;

import com.letsconnect.app.model.enums.KycStatus;
import com.letsconnect.app.model.enums.UserRole;
import com.letsconnect.app.repository.ListenerProfileRepository;
import com.letsconnect.app.repository.UserRepository;
import com.letsconnect.app.service.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/dev")
@RequiredArgsConstructor
@Profile("dev")
public class DevToolsController {
    private final AuthenticatedUser authenticatedUser;
    private final ListenerProfileRepository listenerProfileRepository;
    private final UserRepository userRepository;

    @PostMapping("/listener/verify")
    @Transactional
    public ResponseEntity<?> verifyCurrentListener() {
        var userId = authenticatedUser.currentUserId();
        var profile = listenerProfileRepository.findByUserId(userId).orElse(null);
        if (profile == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Submit a listener application first"));
        }
        profile.setKycStatus(KycStatus.VERIFIED);
        listenerProfileRepository.save(profile);
        var user = profile.getUser();
        user.setRole(UserRole.LISTENER);
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("status", "VERIFIED", "userId", userId));
    }
}
