package com.nexus.crew.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record RegisterArrivalRequest(
    @NotNull(message = "Ship ID is required")
    Long shipId,
    
    @NotNull(message = "Crew count is required")
    @Min(value = 1, message = "Crew count must be at least 1")
    Integer crewCount
) {}
