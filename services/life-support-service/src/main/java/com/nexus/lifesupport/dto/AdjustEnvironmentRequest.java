package com.nexus.lifesupport.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record AdjustEnvironmentRequest(
    @Min(value = 18, message = "Target O2 must be at least 18%")
    @Max(value = 23, message = "Target O2 must not exceed 23%")
    Double targetO2,
    
    @Min(value = 15, message = "Target temperature must be at least 15C")
    @Max(value = 30, message = "Target temperature must not exceed 30C")
    Double targetTemperature,
    
    @Min(value = 95, message = "Target pressure must be at least 95 kPa")
    @Max(value = 105, message = "Target pressure must not exceed 105 kPa")
    Double targetPressure,
    
    @Min(value = 30, message = "Target humidity must be at least 30%")
    @Max(value = 70, message = "Target humidity must not exceed 70%")
    Double targetHumidity
) {}
