package com.trendradar.infrastructure.ebay;

import java.math.BigDecimal;
import java.net.URI;
import java.time.Instant;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class EbayBrowseClient {

    private final EbayProperties properties;
    private final RestClient restClient;

    public EbayBrowseClient(EbayProperties properties) {
        this.properties = properties;
        this.restClient = RestClient.create();
    }

    public EbaySearchResponse search(String query) {
        String accessToken = fetchAccessToken();
        URI uri = UriComponentsBuilder.fromUriString(properties.searchUrl())
            .queryParam("q", query)
            .queryParam("limit", properties.limit())
            .build()
            .encode()
            .toUri();

        return restClient.get()
            .uri(uri)
            .headers(headers -> {
                headers.setBearerAuth(accessToken);
                headers.setAccept(List.of(MediaType.APPLICATION_JSON));
                headers.set("X-EBAY-C-MARKETPLACE-ID", properties.marketplaceId());
            })
            .retrieve()
            .body(EbaySearchResponse.class);
    }

    private String fetchAccessToken() {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "client_credentials");
        formData.add("scope", properties.scope());

        EbayTokenResponse tokenResponse = restClient.post()
            .uri(properties.tokenUrl())
            .headers(headers -> {
                headers.setBasicAuth(properties.username(), properties.password());
                headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
                headers.setAccept(List.of(MediaType.APPLICATION_JSON));
            })
            .body(formData)
            .retrieve()
            .body(EbayTokenResponse.class);

        if (tokenResponse == null || tokenResponse.accessToken() == null || tokenResponse.accessToken().isBlank()) {
            throw new IllegalStateException("eBay token response did not include an access token");
        }

        return tokenResponse.accessToken();
    }

    public record EbayTokenResponse(
        @com.fasterxml.jackson.annotation.JsonProperty("access_token") String accessToken,
        @com.fasterxml.jackson.annotation.JsonProperty("expires_in") long expiresIn,
        @com.fasterxml.jackson.annotation.JsonProperty("token_type") String tokenType
    ) {
    }

    public record EbaySearchResponse(
        String href,
        int total,
        String next,
        int limit,
        int offset,
        List<EbayItemSummary> itemSummaries
    ) {
    }

    public record EbayItemSummary(
        String itemId,
        String title,
        List<EbayCategory> categories,
        EbayImage image,
        EbayPrice price,
        String condition,
        EbaySeller seller,
        List<EbayShippingOption> shippingOptions,
        List<String> buyingOptions,
        String itemWebUrl,
        EbayItemLocation itemLocation,
        boolean topRatedBuyingExperience,
        boolean priorityListing,
        Instant itemOriginDate,
        Instant itemCreationDate
    ) {
    }

    public record EbayCategory(String categoryId, String categoryName) {
    }

    public record EbayImage(String imageUrl) {
    }

    public record EbayPrice(
        BigDecimal value,
        String currency,
        BigDecimal convertedFromValue,
        String convertedFromCurrency
    ) {
    }

    public record EbaySeller(
        String username,
        String feedbackPercentage,
        Integer feedbackScore
    ) {
    }

    public record EbayShippingOption(
        String shippingCostType,
        EbayPrice shippingCost
    ) {
    }

    public record EbayItemLocation(
        String postalCode,
        String country
    ) {
    }
}
