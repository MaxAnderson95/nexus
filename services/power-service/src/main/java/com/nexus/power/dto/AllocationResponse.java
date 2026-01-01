package com.nexus.power.dto;

public record AllocationResponse(
    Long id,
    String systemName,
    Double allocatedKw,
    Integer priority,
    Long sectionId,
    String message
) {}
