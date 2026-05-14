package com.trendradar.infrastructure.ebay;

import com.trendradar.domain.Niche;
import com.trendradar.domain.Region;
import com.trendradar.provider.MarketSignalBatch;
import com.trendradar.provider.MarketSignalProvider;
import com.trendradar.provider.MarketplaceProductSignal;
import java.time.Instant;
import java.util.List;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(0)
public class EbayMarketSignalProvider implements MarketSignalProvider {

    private final EbayProperties properties;
    private final EbayBrowseClient ebayBrowseClient;

    public EbayMarketSignalProvider(EbayProperties properties, EbayBrowseClient ebayBrowseClient) {
        this.properties = properties;
        this.ebayBrowseClient = ebayBrowseClient;
    }

    @Override
    public boolean isAvailable() {
        return properties.isConfigured();
    }

    @Override
    public MarketSignalBatch fetchSignals(Niche niche, Region region) {
        String query = toSearchQuery(niche);
        EbayBrowseClient.EbaySearchResponse response = ebayBrowseClient.search(query);
        List<EbayBrowseClient.EbayItemSummary> summaries = response == null || response.itemSummaries() == null
            ? List.of()
            : response.itemSummaries();

        return new MarketSignalBatch(
            "ebay_browse",
            query,
            response == null ? 0 : response.total(),
            summaries.stream()
                .map(this::toMarketplaceSignal)
                .toList()
        );
    }

    private MarketplaceProductSignal toMarketplaceSignal(EbayBrowseClient.EbayItemSummary item) {
        EbayBrowseClient.EbayCategory category = item.categories() == null || item.categories().isEmpty()
            ? null
            : item.categories().get(0);

        return new MarketplaceProductSignal(
            item.itemId(),
            item.title(),
            category == null ? "Marketplace item" : category.categoryName(),
            item.image() == null ? null : item.image().imageUrl(),
            item.itemWebUrl(),
            item.price() == null ? null : item.price().value(),
            item.price() == null ? null : item.price().currency(),
            item.condition(),
            item.seller() == null ? null : item.seller().username(),
            item.seller() == null ? null : item.seller().feedbackPercentage(),
            item.itemLocation() == null ? null : item.itemLocation().country(),
            item.topRatedBuyingExperience(),
            item.priorityListing(),
            item.itemCreationDate() == null ? Instant.now() : item.itemCreationDate()
        );
    }

    private String toSearchQuery(Niche niche) {
        return switch (niche.code()) {
            case "anime_collectibles" -> "slime anime figure";
            case "fitness_accessories" -> "hydration running belt";
            default -> niche.displayName();
        };
    }
}
