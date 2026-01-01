package com.nexus.crew.dto;

import com.nexus.crew.entity.Section;

import java.time.Instant;

public record SectionDto(
    Long id,
    String name,
    Integer deck,
    Integer maxCapacity,
    Integer currentOccupancy,
    Double occupancyPercent,
    Instant createdAt,
    Instant updatedAt
) {
    public static SectionDto fromEntity(Section entity) {
        double occupancyPercent = entity.getMaxCapacity() > 0 
            ? (entity.getCurrentOccupancy().doubleValue() / entity.getMaxCapacity()) * 100 
            : 0;
        
        return new SectionDto(
            entity.getId(),
            entity.getName(),
            entity.getDeck(),
            entity.getMaxCapacity(),
            entity.getCurrentOccupancy(),
            occupancyPercent,
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }
}
