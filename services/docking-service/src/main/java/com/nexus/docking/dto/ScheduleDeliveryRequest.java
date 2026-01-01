package com.nexus.docking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record ScheduleDeliveryRequest(
    @NotBlank(message = "Ship name is required")
    String shipName,
    
    @NotBlank(message = "Cargo type is required")
    String cargoType,
    
    @NotNull(message = "Estimated arrival time is required")
    Instant estimatedArrival
) {}
