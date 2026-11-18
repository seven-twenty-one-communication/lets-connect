package com.connecto.app.repository;

import com.connecto.app.model.entity.Gift;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GiftRepository extends JpaRepository<Gift, Integer> {
    List<Gift> findByIsActiveTrueOrderByCoinPriceAsc();
}
