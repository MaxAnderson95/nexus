package com.nexus.docking.dto;

import com.nexus.docking.entity.DockingLog;

import java.time.Instant;

public record DockingLogDto(
    Long id,
    Long shipId,
    Long bayId,
    String action,
    Instant timestamp
) {
    public static DockingLogDto fromEntity(DockingLog log) {
        return new DockingLogDto(
            log.getId(),
            log.getShipId(),
            log.getBayId(),
            log.getAction().name(),
            log.getTimestamp()
        );
    }
}
