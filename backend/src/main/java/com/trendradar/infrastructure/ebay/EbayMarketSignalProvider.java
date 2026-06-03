package com.trendradar.infrastructure.ebay;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private final ObjectMapper objectMapper;

    public EbayMarketSignalProvider(
        EbayProperties properties,
        EbayBrowseClient ebayBrowseClient,
        ObjectMapper objectMapper
    ) {
        this.properties = properties;
        this.ebayBrowseClient = ebayBrowseClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean isAvailable() {
        return properties.isConfigured();
    }

    @Override
    public String sourceType() {
        return "ebay_browse";
    }

    @Override
    public String queryFor(Niche niche, Region region) {
        return toSearchQuery(niche);
    }

    @Override
    public String queryFor(Niche niche, Region region, String searchTerm) {
        if (searchTerm == null || searchTerm.isBlank()) {
            return queryFor(niche, region);
        }

        return searchTerm;
    }

    @Override
    public MarketSignalBatch fetchSignals(Niche niche, Region region) {
        String query = queryFor(niche, region);
        return fetchSignals(query);
    }

    @Override
    public MarketSignalBatch fetchSignals(Niche niche, Region region, String searchTerm) {
        String query = queryFor(niche, region, searchTerm);
        return fetchSignals(query);
    }

    private MarketSignalBatch fetchSignals(String query) {
        EbayBrowseClient.EbaySearchResponse response = ebayBrowseClient.search(query);
        List<EbayBrowseClient.EbayItemSummary> summaries = response == null || response.itemSummaries() == null
            ? List.of()
            : response.itemSummaries();

        return new MarketSignalBatch(
            sourceType(),
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
            toRawJson(item),
            item.price() == null ? null : item.price().value(),
            item.price() == null ? null : item.price().currency(),
            item.condition(),
            item.seller() == null ? null : item.seller().username(),
            item.seller() == null ? null : item.seller().feedbackPercentage(),
            item.seller() == null ? null : item.seller().feedbackScore(),
            item.itemLocation() == null ? null : item.itemLocation().country(),
            firstShippingCost(item),
            firstShippingCostType(item),
            item.buyingOptions() == null ? List.of() : item.buyingOptions(),
            item.topRatedBuyingExperience(),
            item.priorityListing(),
            item.itemOriginDate(),
            item.itemCreationDate() == null ? Instant.now() : item.itemCreationDate()
        );
    }

    private String toRawJson(EbayBrowseClient.EbayItemSummary item) {
        try {
            return objectMapper.writeValueAsString(item);
        } catch (JsonProcessingException exception) {
            return "{\"serializationError\":\"%s\"}".formatted(exception.getMessage());
        }
    }

    private java.math.BigDecimal firstShippingCost(EbayBrowseClient.EbayItemSummary item) {
        if (item.shippingOptions() == null || item.shippingOptions().isEmpty()) {
            return null;
        }

        EbayBrowseClient.EbayShippingOption option = item.shippingOptions().get(0);

        return option.shippingCost() == null ? null : option.shippingCost().value();
    }

    private String firstShippingCostType(EbayBrowseClient.EbayItemSummary item) {
        if (item.shippingOptions() == null || item.shippingOptions().isEmpty()) {
            return null;
        }

        return item.shippingOptions().get(0).shippingCostType();
    }

    private String toSearchQuery(Niche niche) {
        return switch (niche.code()) {
            case "anime_collectibles" -> "slime anime figure";
            case "fitness_accessories" -> "hydration running belt";
            default -> niche.displayName();
        };
    }
}
