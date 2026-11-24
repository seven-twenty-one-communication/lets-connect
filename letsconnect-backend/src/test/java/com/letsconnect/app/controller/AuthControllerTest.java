package com.letsconnect.app.controller;

import com.letsconnect.app.model.entity.User;
import com.letsconnect.app.model.entity.Wallet;
import com.letsconnect.app.repository.UserRepository;
import com.letsconnect.app.repository.WalletRepository;
import com.letsconnect.app.repository.AppSessionRepository;
import com.letsconnect.app.service.OtpService;
import com.letsconnect.app.service.AuthenticatedUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class AuthControllerTest {
    private UserRepository userRepository;
    private WalletRepository walletRepository;
    private OtpService otpService;
    private AppSessionRepository appSessionRepository;
    private AuthenticatedUser authenticatedUser;
    private AuthController controller;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        walletRepository = mock(WalletRepository.class);
        otpService = mock(OtpService.class);
        appSessionRepository = mock(AppSessionRepository.class);
        authenticatedUser = mock(AuthenticatedUser.class);
        controller = new AuthController(
                userRepository,
                walletRepository,
                otpService,
                appSessionRepository,
                authenticatedUser
        );
        ReflectionTestUtils.setField(
                controller,
                "jwtSecret",
                "test-secret-with-more-than-thirty-two-bytes-for-hmac"
        );
    }

    @Test
    void newAccountDoesNotReceiveUnapprovedBonusCoinsOrAssumedLanguages() {
        when(otpService.verifyOtp("+919876543210", "123456")).thenReturn(true);
        when(userRepository.findByPhoneNumber("+919876543210")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(UUID.randomUUID());
            return user;
        });
        when(walletRepository.findByUserId(any())).thenReturn(Optional.empty());

        AuthController.VerifyOtpRequest request = new AuthController.VerifyOtpRequest();
        request.setPhoneNumber("+919876543210");
        request.setOtp("123456");

        var response = controller.verifyOtp(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(userRepository).save(argThat(user -> user.getPreferredLanguages().isEmpty()));
        verify(walletRepository).save(argThat(wallet ->
                wallet.getCoinBalance() == 0L && wallet.getEarnedCoinsBalance() == 0L
        ));
        assertTrue(((java.util.Map<?, ?>) response.getBody()).containsKey("token"));
    }

    @Test
    void invalidOtpDoesNotCreateAnAccountOrWallet() {
        when(otpService.verifyOtp("+919876543210", "000000")).thenReturn(false);

        AuthController.VerifyOtpRequest request = new AuthController.VerifyOtpRequest();
        request.setPhoneNumber("+919876543210");
        request.setOtp("000000");

        var response = controller.verifyOtp(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verifyNoInteractions(userRepository, walletRepository);
    }

    @Test
    void sendOtpNeverReturnsDebugCredentials() {
        when(otpService.sendOtp("+919876543210")).thenReturn("123456");
        AuthController.SendOtpRequest request = new AuthController.SendOtpRequest();
        request.setPhoneNumber("+919876543210");

        var response = controller.sendOtp(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(!((java.util.Map<?, ?>) response.getBody()).containsKey("debugOtp"));
        verify(otpService).sendOtp("+919876543210");
    }
}
