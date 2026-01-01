package com.nexus.docking.dto;

import com.nexus.docking.entity.Ship;

import java.time.Instant;

public record ShipDto(
    Long id,
    String name,
    String type,
    Integer crewCount,
    Integer cargoCapacity,
    String status,
    Instant arrivalTime,
    Instant createdAt,
    Instant updatedAt
) {
    public static ShipDto fromEntity(Ship ship) {
        return new ShipDto(
            ship.getId(),
            ship.getName(),
            ship.getType().name(),
            ship.getCrewCount(),
            ship.getCargoCapacity(),
            ship.getStatus().name(),
            ship.getArrivalTime(),
            ship.getCreatedAt(),
            ship.getUpdatedAt()
        );
    }
}
