package com.trendradar.application;

import com.trendradar.domain.Region;
import com.trendradar.infrastructure.persistence.SeedTermEntity;
import com.trendradar.infrastructure.persistence.SeedTermRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SeedTermService {

    private final SeedTermRepository seedTermRepository;

    public SeedTermService(SeedTermRepository seedTermRepository) {
        this.seedTermRepository = seedTermRepository;
    }

    @Transactional(readOnly = true)
    public List<SeedTermResponse> findSeedTerms(String niche, String region) {
        if (hasText(niche) && hasText(region)) {
            return seedTermRepository.findByNicheAndRegionOrderByPriorityDescCreatedAtAsc(
                    niche.trim(),
                    Region.fromCode(region).code()
                )
                .stream()
                .map(this::toResponse)
                .toList();
        }

        return seedTermRepository.findByOrderByPriorityDescCreatedAtAsc()
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional
    public SeedTermResponse createSeedTerm(SeedTermRequest request) {
        SeedTermEntity seedTerm = new SeedTermEntity();
        applyRequiredFields(seedTerm, request);
        seedTerm.setEnabled(request.enabled() == null || request.enabled());
        seedTerm.setPriority(request.priority() == null ? 100 : request.priority());
        seedTerm.setSourceType(defaultSourceType(request.sourceType()));

        return toResponse(seedTermRepository.save(seedTerm));
    }

    @Transactional
    public SeedTermResponse updateSeedTerm(long id, SeedTermRequest request) {
        SeedTermEntity seedTerm = seedTermRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Seed term %d was not found".formatted(id)));

        if (hasText(request.niche())) {
            seedTerm.setNiche(request.niche().trim());
        }
        if (hasText(request.region())) {
            seedTerm.setRegion(Region.fromCode(request.region()).code());
        }
        if (hasText(request.searchTerm())) {
            seedTerm.setSearchTerm(request.searchTerm().trim());
        }
        if (request.enabled() != null) {
            seedTerm.setEnabled(request.enabled());
        }
        if (request.priority() != null) {
            seedTerm.setPriority(request.priority());
        }
        if (hasText(request.sourceType())) {
            seedTerm.setSourceType(request.sourceType().trim());
        }

        return toResponse(seedTermRepository.save(seedTerm));
    }

    @Transactional
    public void deleteSeedTerm(long id) {
        if (!seedTermRepository.existsById(id)) {
            throw new IllegalArgumentException("Seed term %d was not found".formatted(id));
        }

        seedTermRepository.deleteById(id);
    }

    SeedTermResponse toResponse(SeedTermEntity seedTerm) {
        return new SeedTermResponse(
            seedTerm.getId(),
            seedTerm.getNiche(),
            seedTerm.getRegion(),
            seedTerm.getSearchTerm(),
            seedTerm.isEnabled(),
            seedTerm.getPriority(),
            seedTerm.getSourceType(),
            seedTerm.getCreatedAt(),
            seedTerm.getUpdatedAt()
        );
    }

    private void applyRequiredFields(SeedTermEntity seedTerm, SeedTermRequest request) {
        if (!hasText(request.niche())) {
            throw new IllegalArgumentException("niche is required");
        }
        if (!hasText(request.region())) {
            throw new IllegalArgumentException("region is required");
        }
        if (!hasText(request.searchTerm())) {
            throw new IllegalArgumentException("searchTerm is required");
        }

        seedTerm.setNiche(request.niche().trim());
        seedTerm.setRegion(Region.fromCode(request.region()).code());
        seedTerm.setSearchTerm(request.searchTerm().trim());
    }

    private String defaultSourceType(String sourceType) {
        return hasText(sourceType) ? sourceType.trim() : "ebay_browse";
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
