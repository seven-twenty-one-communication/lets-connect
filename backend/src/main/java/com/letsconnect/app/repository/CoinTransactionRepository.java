package com.letsconnect.app.repository;

import com.letsconnect.app.model.entity.CoinTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CoinTransactionRepository extends JpaRepository<CoinTransaction, UUID> {
    List<CoinTransaction> findByUserIdOrderByCreatedAtDesc(UUID userId);
}
