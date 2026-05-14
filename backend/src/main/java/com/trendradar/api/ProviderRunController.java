package com.trendradar.api;

import com.trendradar.application.ProviderRunPageResponse;
import com.trendradar.application.ProviderRunQueryService;
import com.trendradar.application.ProviderRunResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class ProviderRunController {

    private final ProviderRunQueryService providerRunQueryService;

    public ProviderRunController(ProviderRunQueryService providerRunQueryService) {
        this.providerRunQueryService = providerRunQueryService;
    }

    @GetMapping("/api/provider-runs")
    public ProviderRunPageResponse getProviderRuns(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        return providerRunQueryService.findProviderRuns(page, size);
    }

    @GetMapping("/api/provider-runs/{id}")
    public ProviderRunResponse getProviderRun(@PathVariable long id) {
        try {
            return providerRunQueryService.findProviderRun(id);
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, exception.getMessage(), exception);
        }
    }
}
