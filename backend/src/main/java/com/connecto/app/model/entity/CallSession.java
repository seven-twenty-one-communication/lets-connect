package com.connecto.app.model.entity;

import com.connecto.app.model.enums.CallStatus;
import com.connecto.app.model.enums.DisconnectReason;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "call_sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CallSession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "channel_name", nullable = false, unique = true, length = 100)
    private String channelName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "caller_id", nullable = false)
    private User caller;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "listener_id", nullable = false)
    private User listener;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private CallStatus status = CallStatus.INITIATED;

    @Column(name = "rate_per_minute_coins", nullable = false)
    private Integer ratePerMinuteCoins;

    @CreationTimestamp
    @Column(name = "initiated_at", updatable = false)
    private ZonedDateTime initiatedAt;

    @Column(name = "ringing_at")
    private ZonedDateTime ringingAt;

    @Column(name = "connected_at")
    private ZonedDateTime connectedAt;

    @Column(name = "last_billed_at")
    private ZonedDateTime lastBilledAt;

    @Column(name = "ended_at")
    private ZonedDateTime endedAt;

    @Column(name = "duration_seconds")
    @Builder.Default
    private Integer durationSeconds = 0;

    @Column(name = "total_coins_charged")
    @Builder.Default
    private Long totalCoinsCharged = 0L;

    @Column(name = "listener_coins_earned")
    @Builder.Default
    private Long listenerCoinsEarned = 0L;

    @Column(name = "platform_commission_coins")
    @Builder.Default
    private Long platformCommissionCoins = 0L;

    @Enumerated(EnumType.STRING)
    @Column(name = "disconnect_reason", length = 30)
    private DisconnectReason disconnectReason;
}
