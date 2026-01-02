package com.nexus.docking.dto;

public record DockResult(
    boolean success,
    Long bayId,
    Long shipId,
    String message,
    String downstreamService,
    String innerError
) {
    public static DockResult success(Long bayId, Long shipId, String message) {
        return new DockResult(true, bayId, shipId, message, null, null);
    }

    public static DockResult failure(Long shipId, String message) {
        return new DockResult(false, null, shipId, message, null, null);
    }

    public static DockResult downstreamFailure(Long shipId, String message, String downstreamService, String innerError) {
        return new DockResult(false, null, shipId, message, downstreamService, innerError);
    }

    public boolean isDownstreamError() {
        return downstreamService != null;
    }
}
