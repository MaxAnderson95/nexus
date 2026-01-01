package com.nexus.inventory.dto;

import com.nexus.inventory.entity.ResupplyRequest;

import java.time.Instant;

public record ResupplyRequestDto(
    Long id,
    Long supplyId,
    String supplyName,
    Integer quantity,
    String status,
    Instant requestedAt,
    Instant completedAt
) {
    public static ResupplyRequestDto fromEntity(ResupplyRequest request) {
        return new ResupplyRequestDto(
            request.getId(),
            request.getSupplyId(),
            request.getSupplyName(),
            request.getQuantity(),
            request.getStatus().name(),
            request.getRequestedAt(),
            request.getCompletedAt()
        );
    }
}
