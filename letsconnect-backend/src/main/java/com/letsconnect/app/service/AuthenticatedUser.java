package com.letsconnect.app.service;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class AuthenticatedUser {
    public UUID require(UUID requestedUserId) {
        UUID authenticatedUserId = currentUserId();
        if (!authenticatedUserId.equals(requestedUserId)) {
            throw new AccessDeniedException("The authenticated user cannot act for another user");
        }
        return authenticatedUserId;
    }

    public UUID currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Authentication is required");
        }
        try {
            return UUID.fromString(authentication.getName());
        } catch (IllegalArgumentException ex) {
            throw new AccessDeniedException("Invalid authenticated user", ex);
        }
    }

    public void requireParticipant(UUID userA, UUID userB) {
        UUID authenticatedUserId = currentUserId();
        if (!authenticatedUserId.equals(userA) && !authenticatedUserId.equals(userB)) {
            throw new AccessDeniedException("Only a call participant can perform this action");
        }
    }
}
