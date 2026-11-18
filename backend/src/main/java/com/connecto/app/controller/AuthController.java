package com.connecto.app.controller;

import com.connecto.app.model.entity.User;
import com.connecto.app.model.entity.Wallet;
import com.connecto.app.model.enums.UserRole;
import com.connecto.app.model.enums.UserStatus;
import com.connecto.app.repository.UserRepository;
import com.connecto.app.repository.WalletRepository;
import com.connecto.app.service.OtpService;
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
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final OtpService otpService;

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration-ms:2592000000}")
    private long jwtExpirationMs;

    @PostMapping("/send-otp")
    public ResponseEntity<?> sendOtp(@Valid @RequestBody SendOtpRequest request) {
        String otp = otpService.sendOtp(request.getPhoneNumber());
        Map<String, Object> response = new HashMap<>(Map.of(
                "status", "SUCCESS",
                "message", "OTP sent successfully"
        ));
        if (otp != null) {
            response.put("debugOtp", otp);
        }
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
                    .preferredLanguages(List.of("English", "Hindi"))
                    .build();
            user = userRepository.save(user);

            Wallet wallet = Wallet.builder()
                    .user(user)
                    .coinBalance(20L)
                    .earnedCoinsBalance(0L)
                    .build();
            walletRepository.save(wallet);
        }

        Key key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        String jwtToken = Jwts.builder()
                .subject(user.getId().toString())
                .claim("phone", user.getPhoneNumber())
                .claim("role", user.getRole().name())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(key)
                .compact();

        Long currentBalance = walletRepository.findByUserId(user.getId())
                .map(Wallet::getCoinBalance)
                .orElse(0L);

        return ResponseEntity.ok(Map.of(
                "token", jwtToken,
                "userId", user.getId(),
                "nickname", user.getNickname(),
                "avatarUrl", user.getAvatarUrl(),
                "isNewUser", isNewUser,
                "coinBalance", currentBalance
        ));
    }

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
}
