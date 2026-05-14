package com.trendradar.infrastructure.ebay;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "trendradar.ebay")
public record EbayProperties(
    boolean enabled,
    String tokenUrl,
    String searchUrl,
    String username,
    String password,
    String scope,
    int limit,
    String marketplaceId
) {

    public boolean isConfigured() {
        return enabled
            && username != null
            && !username.isBlank()
            && password != null
            && !password.isBlank();
    }
}
