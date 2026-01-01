package com.nexus.power.dto;

import jakarta.validation.constraints.NotBlank;

public record DeallocateRequest(
    @NotBlank(message = "System name is required")
    String system
) {}
