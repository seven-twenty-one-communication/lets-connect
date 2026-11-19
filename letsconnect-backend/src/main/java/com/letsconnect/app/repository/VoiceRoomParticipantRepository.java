package com.letsconnect.app.repository;

import com.letsconnect.app.model.entity.VoiceRoomParticipant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VoiceRoomParticipantRepository extends JpaRepository<VoiceRoomParticipant, UUID> {
    Optional<VoiceRoomParticipant> findByRoomIdAndUserId(UUID roomId, UUID userId);
    List<VoiceRoomParticipant> findByRoomIdAndActiveTrueOrderByJoinedAtAsc(UUID roomId);
    long countByRoomIdAndActiveTrue(UUID roomId);
    boolean existsByRoomIdAndUserIdAndActiveTrue(UUID roomId, UUID userId);
}
