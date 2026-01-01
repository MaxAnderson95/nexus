package com.nexus.lifesupport.dto;

public record EnvironmentSummary(
    Integer totalSections,
    Integer sectionsNominal,
    Integer sectionsWarning,
    Integer sectionsCritical,
    Double averageO2Level,
    Double averageTemperature,
    Double averagePressure,
    Integer activeAlerts,
    Integer totalOccupancy,
    Integer totalCapacity
) {}
