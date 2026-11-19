package com.letsconnect.app.service;

import com.letsconnect.app.model.enums.TransactionType;
import com.letsconnect.app.model.entity.PlayPurchase;
import com.letsconnect.app.repository.CoinPackageRepository;
import com.letsconnect.app.repository.PlayPurchaseRepository;
import com.letsconnect.app.repository.UserRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class GooglePlayBillingService {

    private final WalletService walletService;
    private final CoinPackageRepository coinPackageRepository;
    private final PlayPurchaseRepository playPurchaseRepository;
    private final UserRepository userRepository;
    private final EntityManager entityManager;

    @Value("${google.play.public-license-key:}")
    private String base64EncodedPublicKey;

    @Value("${integrations.mock.play-billing:false}")
    private boolean mockBilling;

    /**
     * Verifies Google Play In-App Purchase receipt cryptographically and credits coins.
     * Prevents duplicate redemption attacks by verifying orderId is unused.
     *
     * @param userId The purchasing user ID
     * @param signedData JSON receipt string from Google Play Billing
     * @param signature Cryptographic RSA signature from Google Play
     * @param productId The Play Console Product ID (e.g. 'com.connecto.coins_120')
     * @param orderId Google Order ID (e.g. 'GPA.1234-5678-9012-34567')
     * @return true if verified and coins credited
     */
    @Transactional
    public boolean verifyAndCreditPurchase(
            UUID userId,
            String signedData,
            String signature,
            String productId,
            String orderId
    ) {
        if (userId == null || orderId == null || orderId.isBlank()
                || productId == null || productId.isBlank()) {
            return false;
        }

        var coinPackage = coinPackageRepository.findByPlayProductIdAndIsActiveTrue(productId).orElse(null);
        if (coinPackage == null || coinPackage.getCoinAmount() <= 0
                || coinPackage.getBonusCoins() < 0) {
            log.warn("Rejecting purchase with unknown product ID {}", productId);
            return false;
        }
        int coinsToCredit = coinPackage.getCoinAmount() + coinPackage.getBonusCoins();

        log.info("Processing Google Play purchase verification for user {} and order {}", userId, orderId);

        boolean alreadyProcessed = playPurchaseRepository.existsByOrderId(orderId)
                || entityManager.createQuery(
                "SELECT COUNT(t) FROM CoinTransaction t WHERE t.referenceId = :orderId", Long.class)
                .setParameter("orderId", orderId)
                .getSingleResult() > 0;

        if (alreadyProcessed) {
            log.warn("Order {} was already credited. Rejecting replay attempt.", orderId);
            return false;
        }

        // 2. Cryptographic RSA Signature Verification
        if (mockBilling) {
            if (!("MOCK_PURCHASE:" + orderId).equals(signedData) || !"MOCK".equals(signature)) {
                log.warn("Rejected malformed mock purchase for order {}", orderId);
                return false;
            }
        } else {
            if (base64EncodedPublicKey.isBlank()
                    || !verifySignature(base64EncodedPublicKey, signedData, signature)) {
                log.error("Google Play purchase signature verification failed for order {}", orderId);
                return false;
            }
        }

        var user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return false;
        }
        playPurchaseRepository.saveAndFlush(PlayPurchase.builder()
                .orderId(orderId)
                .user(user)
                .productId(productId)
                .build());

        // Credit user wallet atomically
        walletService.creditCoins(
                userId,
                coinsToCredit,
                TransactionType.PLAY_PURCHASE,
                orderId,
                "Google Play in-app purchase: " + productId
        );

        log.info("Successfully credited {} coins for order {} to user {}", coinsToCredit, orderId, userId);
        return true;
    }

    private boolean verifySignature(String base64PublicKey, String signedData, String signature) {
        if (signedData == null || signature == null) {
            return false;
        }
        try {
            byte[] decodedKey = Base64.getDecoder().decode(base64PublicKey);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PublicKey publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(decodedKey));

            Signature sig = Signature.getInstance("SHA256withRSA");
            sig.initVerify(publicKey);
            sig.update(signedData.getBytes(StandardCharsets.UTF_8));
            return sig.verify(Base64.getDecoder().decode(signature));
        } catch (Exception e) {
            log.warn("Signature validation encountered exception (normal in dev if using mock key): {}", e.getMessage());
            return false;
        }
    }
}
