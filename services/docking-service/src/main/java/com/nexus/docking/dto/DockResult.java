package com.nexus.docking.dto;

public record DockResult(
    boolean success,
    Long bayId,
    Long shipId,
    String message
) {
    public static DockResult success(Long bayId, Long shipId, String message) {
        return new DockResult(true, bayId, shipId, message);
    }
    
    public static DockResult failure(Long shipId, String message) {
        return new DockResult(false, null, shipId, message);
    }
}
