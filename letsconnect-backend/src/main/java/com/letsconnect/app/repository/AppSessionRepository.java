package com.letsconnect.app.repository;

import com.letsconnect.app.model.entity.AppSession;
import com.letsconnect.app.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AppSessionRepository extends JpaRepository<AppSession, UUID> {
    Optional<AppSession> findByRefreshTokenHash(String refreshTokenHash);
    void deleteByUser(User user);
}
