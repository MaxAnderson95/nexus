package com.nexus.lifesupport.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AdjustCapacityRequest(
    @NotNull(message = "Section ID is required")
    Long sectionId,
    
    @NotNull(message = "Occupancy change is required")
    @Min(value = -100, message = "Occupancy change must be at least -100")
    Integer occupancyChange
) {}
