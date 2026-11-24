package com.letsconnect.app.controller;

import com.letsconnect.app.model.entity.User;
import com.letsconnect.app.model.entity.Wallet;
import com.letsconnect.app.model.entity.AppSession;
import com.letsconnect.app.model.enums.UserRole;
import com.letsconnect.app.model.enums.UserStatus;
import com.letsconnect.app.repository.UserRepository;
import com.letsconnect.app.repository.WalletRepository;
import com.letsconnect.app.repository.AppSessionRepository;
import com.letsconnect.app.service.OtpService;
import com.letsconnect.app.service.AuthenticatedUser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.ZonedDateTime;
import java.util.*;

@Slf4j
@RestController
@RequestMapping({"/api/auth", "/api/v1/auth"})
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final OtpService otpService;
    private final AppSessionRepository appSessionRepository;
    private final AuthenticatedUser authenticatedUser;

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration-ms:2592000000}")
    private long jwtExpirationMs;

    @PostMapping("/send-otp")
    public ResponseEntity<?> sendOtp(@Valid @RequestBody SendOtpRequest request) {
        otpService.sendOtp(request.getPhoneNumber());
        Map<String, Object> response = new HashMap<>(Map.of(
                "status", "SUCCESS",
                "message", "OTP sent successfully"
        ));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        String phone = request.getPhoneNumber();
        if (!otpService.verifyOtp(phone, request.getOtp())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid or expired OTP"));
        }

        boolean isNewUser = false;
        User user = userRepository.findByPhoneNumber(phone).orElse(null);

        if (user == null) {
            isNewUser = true;
            int avatarSeed = new java.security.SecureRandom().nextInt(10000);
            String anonymousNickname = "Friend_" + (1000 + new java.security.SecureRandom().nextInt(9000));

            user = User.builder()
                    .phoneNumber(phone)
                    .nickname(anonymousNickname)
                    .avatarUrl("https://api.dicebear.com/7.x/bottts/png?seed=" + avatarSeed)
                    .role(UserRole.USER)
                    .status(UserStatus.ACTIVE)
                    .preferredLanguages(List.of())
                    .build();
            user = userRepository.save(user);

            Wallet wallet = Wallet.builder()
                    .user(user)
                    .coinBalance(0L)
                    .earnedCoinsBalance(0L)
                    .build();
            walletRepository.save(wallet);
        }

        SessionPair sessionPair = issueSessionPair(user);
        String jwtToken = sessionPair.accessToken();
        String refreshToken = sessionPair.refreshToken();

        Long currentBalance = walletRepository.findByUserId(user.getId())
                .map(Wallet::getCoinBalance)
                .orElse(0L);

        Map<String, Object> response = new HashMap<>();
        response.put("accessToken", jwtToken);
        response.put("token", jwtToken);
        response.put("refreshToken", refreshToken);
        response.put("expiresAt", new Date(System.currentTimeMillis() + jwtExpirationMs).toInstant());
        response.put("user", Map.of(
                "id", user.getId(),
                "nickname", user.getNickname(),
                "avatarUrl", user.getAvatarUrl(),
                "profileState", isNewUser ? "INCOMPLETE" : "ACTIVE"
        ));
        response.put("userId", user.getId());
        response.put("nickname", user.getNickname());
        response.put("avatarUrl", user.getAvatarUrl());
        response.put("isNewUser", isNewUser);
        response.put("coinBalance", currentBalance);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/sessions/refresh")
    public ResponseEntity<?> refreshSession(@Valid @RequestBody RefreshSessionRequest request) {
        String refreshTokenHash = hashRefreshToken(request.getRefreshToken());
        AppSession existing = appSessionRepository.findByRefreshTokenHash(refreshTokenHash).orElse(null);
        if (existing == null
                || existing.getRevokedAt() != null
                || existing.getExpiresAt().isBefore(ZonedDateTime.now())) {
            return ResponseEntity.status(401).body(Map.of("message", "Refresh session is invalid or expired"));
        }
        if (existing.getUser().getStatus() != UserStatus.ACTIVE) {
            return ResponseEntity.status(403).body(Map.of("message", "Account is not active"));
        }

        existing.setRevokedAt(ZonedDateTime.now());
        appSessionRepository.save(existing);
        SessionPair sessionPair = issueSessionPair(existing.getUser());

        Map<String, Object> response = new HashMap<>();
        response.put("accessToken", sessionPair.accessToken());
        response.put("refreshToken", sessionPair.refreshToken());
        response.put("expiresAt", new Date(System.currentTimeMillis() + jwtExpirationMs).toInstant());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/sessions/current")
    public ResponseEntity<?> revokeCurrentSession() {
        UUID userId = authenticatedUser.currentUserId();
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        appSessionRepository.findAll().stream()
                .filter(session -> session.getUser().getId().equals(userId) && session.getRevokedAt() == null)
                .forEach(session -> {
                    session.setRevokedAt(ZonedDateTime.now());
                    appSessionRepository.save(session);
                });
        return ResponseEntity.ok(Map.of("status", "REVOKED"));
    }

    private SessionPair issueSessionPair(User user) {
        String refreshToken = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(generateRandomBytes(32));
        AppSession appSession = AppSession.builder()
                .id(UUID.randomUUID())
                .user(user)
                .refreshTokenHash(hashRefreshToken(refreshToken))
                .expiresAt(ZonedDateTime.now().plusDays(30))
                .build();
        appSessionRepository.save(appSession);

        Key key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        String jwtToken = Jwts.builder()
                .subject(user.getId().toString())
                .claim("phone", user.getPhoneNumber())
                .claim("role", user.getRole().name())
                .claim("sessionId", appSession.getId().toString())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(key)
                .compact();
        return new SessionPair(jwtToken, refreshToken);
    }

    private byte[] generateRandomBytes(int length) {
        byte[] bytes = new byte[length];
        new SecureRandom().nextBytes(bytes);
        return bytes;
    }

    private String hashRefreshToken(String refreshToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(refreshToken.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte value : hash) {
                hex.append(String.format("%02x", value));
            }
            return hex.toString();
        } catch (Exception exception) {
            throw new IllegalStateException("Refresh token hashing unavailable", exception);
        }
    }

    private record SessionPair(String accessToken, String refreshToken) {}

    @Data
    public static class SendOtpRequest {
        @NotBlank
        @Pattern(regexp = "^\\+?[1-9]\\d{9,14}$")
        private String phoneNumber;
    }

    @Data
    public static class VerifyOtpRequest {
        @NotBlank
        @Pattern(regexp = "^\\+?[1-9]\\d{9,14}$")
        private String phoneNumber;
        @NotBlank
        @Pattern(regexp = "^\\d{6}$")
        private String otp;
    }

    @Data
    public static class RefreshSessionRequest {
        @NotBlank
        private String refreshToken;
    }
}
