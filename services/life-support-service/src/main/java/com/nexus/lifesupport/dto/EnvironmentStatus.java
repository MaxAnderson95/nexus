package com.nexus.lifesupport.dto;

import java.time.Instant;

public record EnvironmentStatus(
    Long sectionId,
    String sectionName,
    Double o2Level,
    Double co2Level,
    Double temperature,
    Double pressure,
    Double humidity,
    Double targetO2,
    Double targetTemperature,
    Double targetPressure,
    Double targetHumidity,
    Integer currentOccupancy,
    Integer maxOccupancy,
    String status,
    Instant lastUpdated
) {
    public static String calculateStatus(
            Double o2Level, Double targetO2,
            Double temperature, Double targetTemp,
            Double pressure, Double targetPressure) {
        
        double o2Variance = Math.abs(o2Level - targetO2);
        double tempVariance = Math.abs(temperature - targetTemp);
        double pressureVariance = Math.abs(pressure - targetPressure);
        
        if (o2Variance > 2 || tempVariance > 5 || pressureVariance > 5) {
            return "CRITICAL";
        } else if (o2Variance > 1 || tempVariance > 2 || pressureVariance > 2) {
            return "WARNING";
        }
        return "NOMINAL";
    }
}
