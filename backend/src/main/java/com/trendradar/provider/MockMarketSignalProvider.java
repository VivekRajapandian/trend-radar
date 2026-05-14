package com.trendradar.provider;

import com.trendradar.domain.Niche;
import com.trendradar.domain.Region;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(100)
public class MockMarketSignalProvider implements MarketSignalProvider {

    @Override
    public MarketSignalBatch fetchSignals(Niche niche, Region region) {
        Instant observedAt = Instant.now();

        return new MarketSignalBatch(
            "marketplace_mock",
            "mock:" + niche.code() + ":" + region.code(),
            1320,
            List.of(
                new MarketplaceProductSignal(
                    "mock-rimuru-figure",
                    "Rimuru figure collectible anime authentic",
                    "Anime figure",
                    null,
                    null,
                    new BigDecimal("42.50"),
                    "CAD",
                    "New",
                    "mock.collectibles",
                    "99.8",
                    4200,
                    "CA",
                    BigDecimal.ZERO,
                    "FIXED",
                    List.of("FIXED_PRICE", "BEST_OFFER"),
                    true,
                    false,
                    observedAt.minusSeconds(24L * 60L * 60L),
                    observedAt
                ),
                new MarketplaceProductSignal(
                    "mock-anime-acrylic-stand",
                    "Anime acrylic stand desk collectible",
                    "Desk collectible",
                    null,
                    null,
                    new BigDecimal("18.75"),
                    "CAD",
                    "New",
                    "mock.anime.goods",
                    "99.1",
                    920,
                    "CA",
                    new BigDecimal("6.95"),
                    "FIXED",
                    List.of("FIXED_PRICE"),
                    true,
                    false,
                    observedAt.minusSeconds(18L * 24L * 60L * 60L),
                    observedAt
                ),
                new MarketplaceProductSignal(
                    "mock-hydration-running-belt",
                    "Hydration running belt lightweight fitness accessory",
                    "Fitness accessory",
                    null,
                    null,
                    new BigDecimal("31.20"),
                    "CAD",
                    "New",
                    "mock.active.store",
                    "98.7",
                    310,
                    "US",
                    new BigDecimal("14.50"),
                    "CALCULATED",
                    List.of("FIXED_PRICE"),
                    false,
                    false,
                    observedAt.minusSeconds(110L * 24L * 60L * 60L),
                    observedAt
                )
            )
        );
    }
}
