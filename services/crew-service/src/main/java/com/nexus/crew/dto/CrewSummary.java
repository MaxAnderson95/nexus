package com.nexus.crew.dto;

public record CrewSummary(
    Long totalCrew,
    Long activeCrew,
    Long onLeaveCrew,
    Long offDutyCrew,
    Long inTransitCrew,
    Integer totalSections,
    Integer totalCapacity,
    Integer totalOccupancy,
    Double overallOccupancyPercent
) {}
