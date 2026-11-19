package com.letsconnect.app.model.entity;

import com.letsconnect.app.model.enums.KycStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "listener_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ListenerProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(name = "voice_intro_url", columnDefinition = "TEXT")
    private String voiceIntroUrl;

    @Column(name = "price_per_minute_coins", nullable = false)
    @Builder.Default
    private Integer pricePerMinuteCoins = 10;

    @Column(name = "payout_percentage", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal payoutPercentage = new BigDecimal("60.00");

    @Column(name = "is_online")
    @Builder.Default
    private Boolean isOnline = false;

    @Column(name = "is_on_call")
    @Builder.Default
    private Boolean isOnCall = false;

    @Column(name = "last_seen_at")
    private ZonedDateTime lastSeenAt;

    @Column(name = "rating_avg", precision = 3, scale = 2)
    @Builder.Default
    private BigDecimal ratingAvg = new BigDecimal("5.00");

    @Column(name = "total_reviews")
    @Builder.Default
    private Integer totalReviews = 0;

    @Column(name = "total_calls_completed")
    @Builder.Default
    private Integer totalCallsCompleted = 0;

    @Column(name = "total_minutes_spoken")
    @Builder.Default
    private Long totalMinutesSpoken = 0L;

    @Enumerated(EnumType.STRING)
    @Column(name = "kyc_status", nullable = false, length = 30)
    @Builder.Default
    private KycStatus kycStatus = KycStatus.NOT_SUBMITTED;

    @Column(name = "payout_upi_id", length = 100)
    private String payoutUpiId;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "listener_categories",
        joinColumns = @JoinColumn(name = "listener_id"),
        inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    @Builder.Default
    private Set<Category> categories = new HashSet<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private ZonedDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private ZonedDateTime updatedAt;
}
