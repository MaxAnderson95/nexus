package com.nexus.power.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AllocationRequest(
    @NotBlank(message = "System name is required")
    String system,
    
    @NotNull(message = "Amount is required")
    @Min(value = 1, message = "Amount must be at least 1 kW")
    Double amountKw,
    
    Long sectionId,
    
    Integer priority
) {
    public AllocationRequest {
        if (priority == null) {
            priority = 5; // Default priority
        }
    }
}
