package com.letsconnect.app.controller;

import com.letsconnect.app.model.entity.User;
import com.letsconnect.app.model.enums.UserRole;
import com.letsconnect.app.model.enums.UserStatus;
import com.letsconnect.app.repository.UserRepository;
import com.letsconnect.app.repository.AppSessionRepository;
import com.letsconnect.app.service.AuthenticatedUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserProfileControllerTest {
    private UserRepository userRepository;
    private AuthenticatedUser authenticatedUser;
    private AppSessionRepository appSessionRepository;
    private UserProfileController controller;
    private UUID userId;
    private User user;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        authenticatedUser = mock(AuthenticatedUser.class);
        appSessionRepository = mock(AppSessionRepository.class);
        controller = new UserProfileController(userRepository, authenticatedUser, appSessionRepository);
        userId = UUID.randomUUID();
        user = User.builder()
                .phoneNumber("+919876543210")
                .nickname("Friend_123")
                .avatarUrl("https://example.com/avatar.png")
                .preferredLanguages(List.of())
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .build();
        user.setId(userId);
        when(authenticatedUser.currentUserId()).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    }

    @Test
    void getMeReturnsOnlyTheAuthenticatedProfile() {
        var response = controller.getMe();
        assertEquals(org.springframework.http.HttpStatus.OK, response.getStatusCode());
        verify(authenticatedUser).currentUserId();
    }

    @Test
    void updateMeMutatesOnlySelfProfileFields() {
        var request = new UserProfileController.UpdateProfileRequest();
        request.setNickname("  New Friend  ");
        request.setPreferredLanguages(List.of(" Hindi ", "", "Hindi"));

        var response = controller.updateMe(request);

        assertEquals(org.springframework.http.HttpStatus.OK, response.getStatusCode());
        assertEquals("New Friend", user.getNickname());
        assertEquals(List.of("Hindi"), user.getPreferredLanguages());
        verify(authenticatedUser).require(userId);
        verify(userRepository).save(user);
    }
}
