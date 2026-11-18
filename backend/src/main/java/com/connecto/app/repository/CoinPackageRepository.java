package com.connecto.app.repository;

import com.connecto.app.model.entity.CoinPackage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CoinPackageRepository extends JpaRepository<CoinPackage, Integer> {
    List<CoinPackage> findByIsActiveTrueOrderByCoinAmountAsc();
    Optional<CoinPackage> findByPlayProductIdAndIsActiveTrue(String playProductId);
}
