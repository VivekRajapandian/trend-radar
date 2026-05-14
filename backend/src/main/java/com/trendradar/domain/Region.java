package com.trendradar.domain;

import java.util.Locale;

public record Region(String code, String displayName) {

    public static Region fromCode(String code) {
        String normalized = code == null || code.isBlank()
            ? "CA"
            : code.toUpperCase(Locale.ROOT);

        return switch (normalized) {
            case "CA" -> new Region("CA", "Canada");
            case "US" -> new Region("US", "United States");
            default -> new Region(normalized, normalized);
        };
    }
}
