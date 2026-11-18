package com.connecto.app.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Entity
@Table(name = "coin_packages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CoinPackage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "play_product_id", nullable = false, unique = true, length = 100)
    private String playProductId;

    @Column(name = "coin_amount", nullable = false)
    private Integer coinAmount;

    @Column(name = "bonus_coins")
    @Builder.Default
    private Integer bonusCoins = 0;

    @Column(name = "price_in_inr", nullable = false, precision = 10, scale = 2)
    private BigDecimal priceInInr;

    @Column(name = "display_title", nullable = false, length = 100)
    private String displayTitle;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private ZonedDateTime createdAt;
}
