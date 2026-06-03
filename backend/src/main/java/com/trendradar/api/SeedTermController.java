package com.trendradar.api;

import com.trendradar.application.SeedTermRequest;
import com.trendradar.application.SeedTermResponse;
import com.trendradar.application.SeedTermService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class SeedTermController {

    private final SeedTermService seedTermService;

    public SeedTermController(SeedTermService seedTermService) {
        this.seedTermService = seedTermService;
    }

    @GetMapping("/api/seed-terms")
    public List<SeedTermResponse> getSeedTerms(
        @RequestParam(required = false) String niche,
        @RequestParam(required = false) String region
    ) {
        return seedTermService.findSeedTerms(niche, region);
    }

    @PostMapping("/api/seed-terms")
    @ResponseStatus(HttpStatus.CREATED)
    public SeedTermResponse createSeedTerm(@RequestBody SeedTermRequest request) {
        try {
            return seedTermService.createSeedTerm(request);
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage(), exception);
        }
    }

    @PatchMapping("/api/seed-terms/{id}")
    public SeedTermResponse updateSeedTerm(@PathVariable long id, @RequestBody SeedTermRequest request) {
        try {
            return seedTermService.updateSeedTerm(id, request);
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, exception.getMessage(), exception);
        }
    }

    @DeleteMapping("/api/seed-terms/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSeedTerm(@PathVariable long id) {
        try {
            seedTermService.deleteSeedTerm(id);
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, exception.getMessage(), exception);
        }
    }
}
