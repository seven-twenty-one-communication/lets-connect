package com.connecto.app.repository;

import com.connecto.app.model.entity.UserBlock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserBlockRepository extends JpaRepository<UserBlock, UUID> {
    Optional<UserBlock> findByBlockerIdAndBlockedId(UUID blockerId, UUID blockedId);
    List<UserBlock> findByBlockerIdOrderByCreatedAtDesc(UUID blockerId);

    @Query("""
            SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END
            FROM UserBlock b
            WHERE (b.blocker.id = :userA AND b.blocked.id = :userB)
               OR (b.blocker.id = :userB AND b.blocked.id = :userA)
            """)
    boolean existsBlockBetween(@Param("userA") UUID userA, @Param("userB") UUID userB);
}
