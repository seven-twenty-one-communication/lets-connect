package com.letsconnect.app.service;

import com.letsconnect.app.model.entity.CoinPackage;
import com.letsconnect.app.repository.CoinPackageRepository;
import com.letsconnect.app.repository.PlayPurchaseRepository;
import com.letsconnect.app.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GooglePlayBillingServiceTest {
    private WalletService walletService;
    private CoinPackageRepository coinPackageRepository;
    private PlayPurchaseRepository playPurchaseRepository;
    private UserRepository userRepository;
    private EntityManager entityManager;
    private GooglePlayBillingService service;

    @BeforeEach
    void setUp() {
        walletService = mock(WalletService.class);
        coinPackageRepository = mock(CoinPackageRepository.class);
        playPurchaseRepository = mock(PlayPurchaseRepository.class);
        userRepository = mock(UserRepository.class);
        entityManager = mock(EntityManager.class);
        service = new GooglePlayBillingService(
                walletService, coinPackageRepository, playPurchaseRepository, userRepository, entityManager
        );
        ReflectionTestUtils.setField(service, "mockBilling", true);
    }

    @Test
    void validMockPurchaseCreditsTheConfiguredPackageAmount() {
        when(coinPackageRepository.findByPlayProductIdAndIsActiveTrue("com.connecto.coins_120"))
                .thenReturn(java.util.Optional.of(CoinPackage.builder()
                        .playProductId("com.connecto.coins_120")
                        .coinAmount(100)
                        .bonusCoins(20)
                        .build()));
        TypedQuery<Long> query = mock(TypedQuery.class);
        when(entityManager.createQuery(anyString(), eq(Long.class))).thenReturn(query);
        when(query.setParameter("orderId", "mock-order-1")).thenReturn(query);
        when(query.getSingleResult()).thenReturn(0L);
        UUID userId = UUID.randomUUID();
        when(playPurchaseRepository.existsByOrderId("mock-order-1")).thenReturn(false);
        when(userRepository.findById(userId)).thenReturn(java.util.Optional.of(
                com.letsconnect.app.model.entity.User.builder().id(userId).build()
        ));

        boolean success = service.verifyAndCreditPurchase(
                userId,
                "MOCK_PURCHASE:mock-order-1",
                "MOCK",
                "com.connecto.coins_120",
                "mock-order-1"
        );

        assertTrue(success);
        verify(walletService).creditCoins(
                userId,
                120,
                com.letsconnect.app.model.enums.TransactionType.PLAY_PURCHASE,
                "mock-order-1",
                "Google Play in-app purchase: com.connecto.coins_120"
        );
    }

    @Test
    void unknownProductsAndDuplicateOrdersAreRejected() {
        when(coinPackageRepository.findByPlayProductIdAndIsActiveTrue("unknown"))
                .thenReturn(java.util.Optional.empty());
        when(coinPackageRepository.findByPlayProductIdAndIsActiveTrue("com.connecto.coins_50"))
                .thenReturn(java.util.Optional.of(CoinPackage.builder()
                        .playProductId("com.connecto.coins_50")
                        .coinAmount(50)
                        .bonusCoins(0)
                        .build()));
        when(playPurchaseRepository.existsByOrderId("used-order")).thenReturn(false);
        when(userRepository.findById(any())).thenReturn(java.util.Optional.empty());
        assertFalse(service.verifyAndCreditPurchase(
                UUID.randomUUID(), "MOCK_PURCHASE:x", "MOCK", "unknown", "x"
        ));

        TypedQuery<Long> query = mock(TypedQuery.class);
        when(entityManager.createQuery(anyString(), eq(Long.class))).thenReturn(query);
        when(query.setParameter("orderId", "used-order")).thenReturn(query);
        when(query.getSingleResult()).thenReturn(1L);
        assertFalse(service.verifyAndCreditPurchase(
                UUID.randomUUID(),
                "MOCK_PURCHASE:used-order",
                "MOCK",
                "com.connecto.coins_50",
                "used-order"
        ));

        verifyNoInteractions(walletService);
    }
}
