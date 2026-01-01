package com.nexus.power.dto;

import java.util.List;

public record PowerGridStatus(
    Double totalCapacityKw,
    Double totalOutputKw,
    Double totalAllocatedKw,
    Double availableKw,
    Double utilizationPercent,
    Integer onlineSources,
    Integer totalSources,
    List<PowerSourceSummary> sources
) {
    public record PowerSourceSummary(
        Long id,
        String name,
        String type,
        String status,
        Double maxOutputKw,
        Double currentOutputKw,
        Double utilizationPercent
    ) {}
}
