package com.trendradar.domain;

public record Niche(String code, String displayName) {

    public static Niche fromCode(String code) {
        String normalized = code == null || code.isBlank() ? "general" : code;

        return switch (normalized) {
            case "anime_collectibles" -> new Niche(normalized, "Anime collectibles");
            case "fitness_accessories" -> new Niche(normalized, "Fitness accessories");
            default -> new Niche(normalized, humanize(normalized));
        };
    }

    private static String humanize(String code) {
        String normalized = code.replace('_', ' ').replace('-', ' ');
        return normalized.substring(0, 1).toUpperCase() + normalized.substring(1);
    }
}
