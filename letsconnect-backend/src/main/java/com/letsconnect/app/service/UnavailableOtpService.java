package com.letsconnect.app.service;

import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@Profile("!dev & !test")
public class UnavailableOtpService implements OtpService {
    @Override
    public String sendOtp(String phoneNumber) {
        throw unavailable();
    }

    @Override
    public boolean verifyOtp(String phoneNumber, String otp) {
        throw unavailable();
    }

    private ResponseStatusException unavailable() {
        return new ResponseStatusException(
                HttpStatus.SERVICE_UNAVAILABLE,
                "OTP provider is not configured for this environment"
        );
    }
}
