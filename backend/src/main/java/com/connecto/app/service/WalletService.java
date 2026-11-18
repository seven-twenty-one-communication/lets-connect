package com.connecto.app.service;

import com.connecto.app.model.entity.CoinTransaction;
import com.connecto.app.model.entity.User;
import com.connecto.app.model.entity.Wallet;
import com.connecto.app.model.enums.TransactionType;
import com.connecto.app.repository.CoinTransactionRepository;
import com.connecto.app.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepository;
    private final CoinTransactionRepository coinTransactionRepository;

    @Transactional(readOnly = true)
    public Long getBalance(UUID userId) {
        return walletRepository.findByUserId(userId)
                .map(Wallet::getCoinBalance)
                .orElse(0L);
    }

    @Transactional
    public Wallet getOrCreateWallet(User user) {
        return walletRepository.findByUserId(user.getId())
                .orElseGet(() -> walletRepository.save(
                        Wallet.builder()
                                .user(user)
                                .coinBalance(0L)
                                .earnedCoinsBalance(0L)
                                .build()
                ));
    }

    @Transactional
    public boolean debitCoins(UUID userId, long coinsToDeduct, TransactionType type, String refId, String description) {
        if (coinsToDeduct <= 0) {
            throw new IllegalArgumentException("Coin amount must be positive");
        }
        Wallet wallet = walletRepository.findByUserIdWithLock(userId)
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found for user: " + userId));

        if (wallet.getCoinBalance() < coinsToDeduct) {
            log.warn("User {} has insufficient coins: has {}, needs {}", userId, wallet.getCoinBalance(), coinsToDeduct);
            return false;
        }

        wallet.setCoinBalance(wallet.getCoinBalance() - coinsToDeduct);
        walletRepository.save(wallet);

        coinTransactionRepository.save(
                CoinTransaction.builder()
                        .wallet(wallet)
                        .user(wallet.getUser())
                        .amount(-coinsToDeduct)
                        .balanceAfter(wallet.getCoinBalance())
                        .transactionType(type)
                        .referenceId(refId)
                        .description(description)
                        .build()
        );

        return true;
    }

    @Transactional
    public void creditCoins(UUID userId, long coinsToCredit, TransactionType type, String refId, String description) {
        if (coinsToCredit <= 0) {
            throw new IllegalArgumentException("Coin amount must be positive");
        }
        Wallet wallet = walletRepository.findByUserIdWithLock(userId)
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found for user: " + userId));

        wallet.setCoinBalance(wallet.getCoinBalance() + coinsToCredit);
        walletRepository.save(wallet);

        coinTransactionRepository.save(
                CoinTransaction.builder()
                        .wallet(wallet)
                        .user(wallet.getUser())
                        .amount(coinsToCredit)
                        .balanceAfter(wallet.getCoinBalance())
                        .transactionType(type)
                        .referenceId(refId)
                        .description(description)
                        .build()
        );
    }

    @Transactional
    public void creditListenerEarnings(UUID listenerUserId, long earnedCoins, String refId, String description) {
        if (earnedCoins < 0) {
            throw new IllegalArgumentException("Earned coin amount must be positive");
        }
        if (earnedCoins == 0) {
            return;
        }
        Wallet wallet = walletRepository.findByUserIdWithLock(listenerUserId)
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found for listener: " + listenerUserId));

        wallet.setEarnedCoinsBalance(wallet.getEarnedCoinsBalance() + earnedCoins);
        walletRepository.save(wallet);

        coinTransactionRepository.save(
                CoinTransaction.builder()
                        .wallet(wallet)
                        .user(wallet.getUser())
                        .amount(earnedCoins)
                        .balanceAfter(wallet.getEarnedCoinsBalance())
                        .transactionType(TransactionType.CALL_CREDIT_EARNING)
                        .referenceId(refId)
                        .description(description)
                        .build()
        );
    }
}
