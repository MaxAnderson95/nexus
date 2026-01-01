package com.nexus.inventory.dto;

import com.nexus.inventory.entity.CargoManifest;
import com.nexus.inventory.entity.CargoItem;

import java.time.Instant;
import java.util.List;

public record CargoManifestDto(
    Long id,
    Long shipId,
    String shipName,
    String status,
    List<CargoItemDto> items,
    Instant createdAt,
    Instant completedAt
) {
    public static CargoManifestDto fromEntity(CargoManifest manifest) {
        List<CargoItemDto> itemDtos = manifest.getItems() != null 
            ? manifest.getItems().stream().map(CargoItemDto::fromEntity).toList()
            : List.of();
        
        return new CargoManifestDto(
            manifest.getId(),
            manifest.getShipId(),
            manifest.getShipName(),
            manifest.getStatus().name(),
            itemDtos,
            manifest.getCreatedAt(),
            manifest.getCompletedAt()
        );
    }
    
    public record CargoItemDto(
        Long id,
        Long supplyId,
        String supplyName,
        Integer quantity
    ) {
        public static CargoItemDto fromEntity(CargoItem item) {
            return new CargoItemDto(
                item.getId(),
                item.getSupplyId(),
                item.getSupplyName(),
                item.getQuantity()
            );
        }
    }
}
