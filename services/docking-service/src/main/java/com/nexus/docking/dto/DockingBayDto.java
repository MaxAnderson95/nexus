package com.nexus.docking.dto;

import com.nexus.docking.entity.DockingBay;

import java.time.Instant;

public record DockingBayDto(
    Long id,
    Integer bayNumber,
    String status,
    Long currentShipId,
    Integer capacity,
    Instant createdAt,
    Instant updatedAt
) {
    public static DockingBayDto fromEntity(DockingBay bay) {
        return new DockingBayDto(
            bay.getId(),
            bay.getBayNumber(),
            bay.getStatus().name(),
            bay.getCurrentShipId(),
            bay.getCapacity(),
            bay.getCreatedAt(),
            bay.getUpdatedAt()
        );
    }
}
