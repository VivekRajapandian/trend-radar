package com.trendradar.application;

import java.util.List;

public record ProviderRunPageResponse(
    List<ProviderRunResponse> runs,
    int page,
    int size,
    long totalElements,
    int totalPages
) {
}
