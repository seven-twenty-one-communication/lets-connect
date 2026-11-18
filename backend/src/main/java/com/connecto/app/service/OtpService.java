package com.connecto.app.service;

public interface OtpService {
    /**
     * Returns a debug OTP only when the active provider is a local mock.
     */
    String sendOtp(String phoneNumber);

    boolean verifyOtp(String phoneNumber, String otp);
}
