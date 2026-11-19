package com.letsconnect.app.repository;

import com.letsconnect.app.model.entity.PlayPurchase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PlayPurchaseRepository extends JpaRepository<PlayPurchase, UUID> {
    boolean existsByOrderId(String orderId);
}
