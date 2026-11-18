package com.connecto.app.service;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
@Profile({"dev", "test"})
public class MockOtpService implements OtpService {
    private static final String MOCK_OTP = "123456";
    private static final long OTP_TTL_SECONDS = 300;

    private final ConcurrentMap<String, Instant> issuedAtByPhone = new ConcurrentHashMap<>();
    private final Clock clock;

    public MockOtpService() {
        this(Clock.systemUTC());
    }

    MockOtpService(Clock clock) {
        this.clock = clock;
    }

    @Override
    public String sendOtp(String phoneNumber) {
        issuedAtByPhone.put(phoneNumber, clock.instant());
        return MOCK_OTP;
    }

    @Override
    public boolean verifyOtp(String phoneNumber, String otp) {
        Instant issuedAt = issuedAtByPhone.get(phoneNumber);
        if (issuedAt == null || issuedAt.plusSeconds(OTP_TTL_SECONDS).isBefore(clock.instant())
                || !MOCK_OTP.equals(otp)) {
            return false;
        }
        issuedAtByPhone.remove(phoneNumber, issuedAt);
        return true;
    }
}
