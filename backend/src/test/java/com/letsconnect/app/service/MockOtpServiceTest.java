package com.letsconnect.app.service;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class MockOtpServiceTest {
    @Test
    void otpMustBeIssuedAndCanOnlyBeUsedOnce() {
        MockOtpService service = new MockOtpService(Clock.systemUTC());

        assertFalse(service.verifyOtp("+919876543210", "123456"));
        assertEquals("123456", service.sendOtp("+919876543210"));
        assertTrue(service.verifyOtp("+919876543210", "123456"));
        assertFalse(service.verifyOtp("+919876543210", "123456"));
    }

    @Test
    void otpExpiresAfterFiveMinutes() {
        AtomicReference<Instant> now = new AtomicReference<>(Instant.parse("2026-01-01T00:00:00Z"));
        Clock clock = new Clock() {
            @Override
            public ZoneId getZone() {
                return ZoneOffset.UTC;
            }

            @Override
            public Clock withZone(ZoneId zone) {
                return this;
            }

            @Override
            public Instant instant() {
                return now.get();
            }
        };
        MockOtpService service = new MockOtpService(clock);
        service.sendOtp("+919876543210");

        now.set(now.get().plusSeconds(301));

        assertFalse(service.verifyOtp("+919876543210", "123456"));
    }
}
