package com.connecto.app.repository;

import com.connecto.app.model.entity.CallSession;
import com.connecto.app.model.enums.CallStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CallSessionRepository extends JpaRepository<CallSession, UUID> {
    Optional<CallSession> findByChannelName(String channelName);
    List<CallSession> findByStatus(CallStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM CallSession c WHERE c.id = :callId")
    Optional<CallSession> findByIdForUpdate(@Param("callId") UUID callId);

    @Query("""
            SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END
            FROM CallSession c
            WHERE c.id = :callId
              AND (c.caller.id = :userId OR c.listener.id = :userId)
            """)
    boolean existsParticipant(@Param("callId") UUID callId, @Param("userId") UUID userId);
}
