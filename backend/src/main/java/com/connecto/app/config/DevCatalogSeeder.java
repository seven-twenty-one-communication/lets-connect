package com.connecto.app.config;

import com.connecto.app.model.entity.Category;
import com.connecto.app.model.entity.CoinPackage;
import com.connecto.app.model.entity.Gift;
import com.connecto.app.repository.CategoryRepository;
import com.connecto.app.repository.CoinPackageRepository;
import com.connecto.app.repository.GiftRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@Profile("dev")
@RequiredArgsConstructor
public class DevCatalogSeeder implements ApplicationRunner {
    private final CategoryRepository categoryRepository;
    private final GiftRepository giftRepository;
    private final CoinPackageRepository coinPackageRepository;

    @Override
    public void run(ApplicationArguments args) {
        if (categoryRepository.count() == 0) {
            categoryRepository.saveAll(List.of(
                    category("Breakup Recovery", "breakup-recovery", 1),
                    category("Relationship Advice", "relationship-advice", 2),
                    category("Work Stress", "work-stress", 3),
                    category("Loneliness Support", "loneliness-support", 4),
                    category("Everyday Conversation", "everyday-conversation", 5)
            ));
        }
        if (giftRepository.count() == 0) {
            giftRepository.saveAll(List.of(
                    gift("Rose", "🌹", 10),
                    gift("Coffee", "☕", 25),
                    gift("Heart", "❤️", 50),
                    gift("Star", "⭐", 100)
            ));
        }
        if (coinPackageRepository.count() == 0) {
            coinPackageRepository.saveAll(List.of(
                    coinPackage("com.connecto.coins_50", 50, "Starter Coins", "49.00"),
                    coinPackage("com.connecto.coins_120", 120, "Popular Coins", "99.00"),
                    coinPackage("com.connecto.coins_350", 350, "Value Coins", "249.00"),
                    coinPackage("com.connecto.coins_800", 800, "Mega Coins", "499.00")
            ));
        }
    }

    private Category category(String name, String slug, int order) {
        return Category.builder().name(name).slug(slug).displayOrder(order).build();
    }

    private Gift gift(String name, String emoji, int price) {
        return Gift.builder().name(name).emoji(emoji).coinPrice(price).build();
    }

    private CoinPackage coinPackage(String productId, int coins, String title, String price) {
        return CoinPackage.builder()
                .playProductId(productId)
                .coinAmount(coins)
                .priceInInr(new BigDecimal(price))
                .displayTitle(title)
                .build();
    }
}
