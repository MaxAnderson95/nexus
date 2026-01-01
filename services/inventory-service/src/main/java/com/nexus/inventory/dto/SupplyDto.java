package com.nexus.inventory.dto;

import com.nexus.inventory.entity.Supply;

import java.time.Instant;

public record SupplyDto(
    Long id,
    String name,
    String category,
    Integer quantity,
    String unit,
    Integer minThreshold,
    Long sectionId,
    boolean lowStock,
    Instant createdAt,
    Instant updatedAt
) {
    public static SupplyDto fromEntity(Supply supply) {
        return new SupplyDto(
            supply.getId(),
            supply.getName(),
            supply.getCategory().name(),
            supply.getQuantity(),
            supply.getUnit(),
            supply.getMinThreshold(),
            supply.getSectionId(),
            supply.isLowStock(),
            supply.getCreatedAt(),
            supply.getUpdatedAt()
        );
    }
}
