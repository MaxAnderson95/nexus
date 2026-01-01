package com.nexus.crew.dto;

import jakarta.validation.constraints.NotNull;

public record RelocateRequest(
    @NotNull(message = "Crew ID is required")
    Long crewId,
    
    @NotNull(message = "Target section ID is required")
    Long targetSectionId
) {}
