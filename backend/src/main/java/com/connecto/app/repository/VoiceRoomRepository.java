package com.connecto.app.repository;

import com.connecto.app.model.entity.VoiceRoom;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VoiceRoomRepository extends JpaRepository<VoiceRoom, UUID> {
    List<VoiceRoom> findByActiveTrueOrderByCreatedAtDesc();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM VoiceRoom r WHERE r.id = :roomId")
    Optional<VoiceRoom> findByIdForUpdate(@Param("roomId") UUID roomId);
}
