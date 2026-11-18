package com.connecto.app.repository;

import com.connecto.app.model.entity.ListenerProfile;
import com.connecto.app.model.enums.KycStatus;
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
public interface ListenerProfileRepository extends JpaRepository<ListenerProfile, UUID> {

    Optional<ListenerProfile> findByUserId(UUID userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT l FROM ListenerProfile l WHERE l.user.id = :userId")
    Optional<ListenerProfile> findByUserIdForUpdate(@Param("userId") UUID userId);

    @Query("""
        SELECT DISTINCT l FROM ListenerProfile l
        JOIN l.user u
        LEFT JOIN l.categories c
        WHERE l.kycStatus = :kycStatus
          AND (:categoryId IS NULL OR c.id = :categoryId)
          AND (:isOnline IS NULL OR l.isOnline = :isOnline)
        ORDER BY l.isOnline DESC, l.ratingAvg DESC
    """)
    List<ListenerProfile> findFilteredListeners(
        @Param("kycStatus") KycStatus kycStatus,
        @Param("categoryId") Integer categoryId,
        @Param("isOnline") Boolean isOnline
    );
}
