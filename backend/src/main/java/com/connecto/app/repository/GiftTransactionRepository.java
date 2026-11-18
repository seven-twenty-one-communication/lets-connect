package com.connecto.app.repository;

import com.connecto.app.model.entity.GiftTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface GiftTransactionRepository extends JpaRepository<GiftTransaction, UUID> {
    List<GiftTransaction> findByRecipientIdOrderByCreatedAtDesc(UUID recipientId);
}
