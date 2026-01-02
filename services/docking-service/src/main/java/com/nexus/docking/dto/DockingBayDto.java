package com.nexus.docking.dto;

import com.nexus.docking.entity.DockingBay;

import java.time.Instant;

public record DockingBayDto(
    Long id,
    Integer bayNumber,
    String status,
    Long currentShipId,
    String currentShipName,
    Integer capacity,
    Instant createdAt,
    Instant updatedAt
) {
    public static DockingBayDto fromEntity(DockingBay bay) {
        return fromEntity(bay, null);
    }
    
    public static DockingBayDto fromEntity(DockingBay bay, String shipName) {
        return new DockingBayDto(
            bay.getId(),
            bay.getBayNumber(),
            bay.getStatus().name(),
            bay.getCurrentShipId(),
            shipName,
            bay.getCapacity(),
            bay.getCreatedAt(),
            bay.getUpdatedAt()
        );
    }
}
