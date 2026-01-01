package com.nexus.lifesupport.service;

import com.nexus.lifesupport.client.PowerClient;
import com.nexus.lifesupport.dto.*;
import com.nexus.lifesupport.entity.Alert;
import com.nexus.lifesupport.entity.EnvironmentalReading;
import com.nexus.lifesupport.entity.EnvironmentalSettings;
import com.nexus.lifesupport.repository.AlertRepository;
import com.nexus.lifesupport.repository.EnvironmentalReadingRepository;
import com.nexus.lifesupport.repository.EnvironmentalSettingsRepository;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class LifeSupportService {
    
    private static final Logger log = LoggerFactory.getLogger(LifeSupportService.class);
    
    private final EnvironmentalReadingRepository readingRepository;
    private final EnvironmentalSettingsRepository settingsRepository;
    private final AlertRepository alertRepository;
    private final PowerClient powerClient;
    private final Tracer tracer;
    private final boolean customSpansEnabled;
    
    public LifeSupportService(
            EnvironmentalReadingRepository readingRepository,
            EnvironmentalSettingsRepository settingsRepository,
            AlertRepository alertRepository,
            PowerClient powerClient,
            Tracer tracer,
            @Value("${nexus.telemetry.custom-spans:false}") boolean customSpansEnabled) {
        this.readingRepository = readingRepository;
        this.settingsRepository = settingsRepository;
        this.alertRepository = alertRepository;
        this.powerClient = powerClient;
        this.tracer = tracer;
        this.customSpansEnabled = customSpansEnabled;
    }
    
    public List<EnvironmentStatus> getAllEnvironmentStatus() {
        if (customSpansEnabled) {
            Span span = tracer.spanBuilder("lifesupport.getAllEnvironmentStatus").startSpan();
            try (Scope scope = span.makeCurrent()) {
                List<EnvironmentStatus> statuses = buildAllEnvironmentStatus();
                span.setAttribute("lifesupport.section_count", statuses.size());
                return statuses;
            } finally {
                span.end();
            }
        }
        return buildAllEnvironmentStatus();
    }
    
    private List<EnvironmentStatus> buildAllEnvironmentStatus() {
        List<EnvironmentalSettings> allSettings = settingsRepository.findAll();
        
        return allSettings.stream().map(settings -> {
            EnvironmentalReading reading = readingRepository
                    .findLatestBySectionId(settings.getSectionId())
                    .orElseGet(() -> createDefaultReading(settings));
            
            return buildEnvironmentStatus(settings, reading);
        }).toList();
    }
    
    public EnvironmentStatus getEnvironmentStatus(Long sectionId) {
        EnvironmentalSettings settings = settingsRepository.findBySectionId(sectionId)
                .orElseThrow(() -> new SectionNotFoundException("Section not found: " + sectionId));
        
        EnvironmentalReading reading = readingRepository
                .findLatestBySectionId(sectionId)
                .orElseGet(() -> createDefaultReading(settings));
        
        return buildEnvironmentStatus(settings, reading);
    }
    
    public EnvironmentSummary getEnvironmentSummary() {
        List<EnvironmentStatus> statuses = buildAllEnvironmentStatus();
        
        int nominal = 0, warning = 0, critical = 0;
        double totalO2 = 0, totalTemp = 0, totalPressure = 0;
        int totalOccupancy = 0, totalCapacity = 0;
        
        for (EnvironmentStatus status : statuses) {
            switch (status.status()) {
                case "NOMINAL" -> nominal++;
                case "WARNING" -> warning++;
                case "CRITICAL" -> critical++;
            }
            totalO2 += status.o2Level();
            totalTemp += status.temperature();
            totalPressure += status.pressure();
            totalOccupancy += status.currentOccupancy();
            totalCapacity += status.maxOccupancy();
        }
        
        int count = statuses.size();
        
        return new EnvironmentSummary(
                count,
                nominal,
                warning,
                critical,
                count > 0 ? totalO2 / count : 0,
                count > 0 ? totalTemp / count : 0,
                count > 0 ? totalPressure / count : 0,
                (int) alertRepository.countByAcknowledgedFalse(),
                totalOccupancy,
                totalCapacity
        );
    }
    
    @Transactional
    public EnvironmentStatus adjustEnvironment(Long sectionId, AdjustEnvironmentRequest request) {
        if (customSpansEnabled) {
            Span span = tracer.spanBuilder("lifesupport.adjustEnvironment")
                    .setAttribute("lifesupport.section_id", sectionId)
                    .startSpan();
            try (Scope scope = span.makeCurrent()) {
                span.addEvent("adjusting_environmental_controls");
                EnvironmentStatus result = performAdjustment(sectionId, request);
                span.addEvent("environment_adjusted");
                span.setAttribute("lifesupport.status", result.status());
                return result;
            } finally {
                span.end();
            }
        }
        return performAdjustment(sectionId, request);
    }
    
    private EnvironmentStatus performAdjustment(Long sectionId, AdjustEnvironmentRequest request) {
        log.info("Adjusting environment for section {}: {}", sectionId, request);
        
        EnvironmentalSettings settings = settingsRepository.findBySectionId(sectionId)
                .orElseThrow(() -> new SectionNotFoundException("Section not found: " + sectionId));
        
        // Request power allocation for environmental adjustment
        try {
            powerClient.allocatePower(
                    "life_support_section_" + sectionId,
                    50.0, // Base power for adjustment
                    sectionId
            );
        } catch (Exception e) {
            log.warn("Could not allocate additional power for adjustment: {}", e.getMessage());
            // Continue with adjustment anyway
        }
        
        // Update settings
        if (request.targetO2() != null) {
            settings.setTargetO2(request.targetO2());
        }
        if (request.targetTemperature() != null) {
            settings.setTargetTemperature(request.targetTemperature());
        }
        if (request.targetPressure() != null) {
            settings.setTargetPressure(request.targetPressure());
        }
        if (request.targetHumidity() != null) {
            settings.setTargetHumidity(request.targetHumidity());
        }
        
        settings = settingsRepository.save(settings);
        
        // Create new reading simulating the adjustment taking effect
        EnvironmentalReading newReading = new EnvironmentalReading();
        newReading.setSectionId(sectionId);
        newReading.setSectionName(settings.getSectionName());
        newReading.setO2Level(settings.getTargetO2() + (Math.random() - 0.5) * 0.2);
        newReading.setCo2Level(0.04 + Math.random() * 0.01);
        newReading.setTemperature(settings.getTargetTemperature() + (Math.random() - 0.5) * 0.5);
        newReading.setPressure(settings.getTargetPressure() + (Math.random() - 0.5) * 0.2);
        newReading.setHumidity(settings.getTargetHumidity() + (Math.random() - 0.5) * 2);
        readingRepository.save(newReading);
        
        log.info("Environment adjusted for section {}", sectionId);
        
        return buildEnvironmentStatus(settings, newReading);
    }
    
    @Transactional
    public void adjustCapacity(AdjustCapacityRequest request) {
        log.info("Adjusting capacity for section {}: change = {}", 
                request.sectionId(), request.occupancyChange());
        
        EnvironmentalSettings settings = settingsRepository.findBySectionId(request.sectionId())
                .orElseThrow(() -> new SectionNotFoundException("Section not found: " + request.sectionId()));
        
        int newOccupancy = settings.getCurrentOccupancy() + request.occupancyChange();
        if (newOccupancy < 0) {
            newOccupancy = 0;
        }
        if (newOccupancy > settings.getMaxOccupancy()) {
            throw new CapacityExceededException(
                    "Cannot exceed max occupancy of " + settings.getMaxOccupancy());
        }
        
        settings.setCurrentOccupancy(newOccupancy);
        settingsRepository.save(settings);
        
        // Request additional power if occupancy increased
        if (request.occupancyChange() > 0) {
            try {
                double additionalPower = request.occupancyChange() * 5.0; // 5kW per person
                powerClient.allocatePower(
                        "life_support_section_" + request.sectionId(),
                        additionalPower,
                        request.sectionId()
                );
            } catch (Exception e) {
                log.warn("Could not allocate additional power for capacity change: {}", e.getMessage());
            }
        }
        
        log.info("Capacity adjusted for section {}: new occupancy = {}", 
                request.sectionId(), newOccupancy);
    }
    
    public List<Alert> getAlerts() {
        return alertRepository.findByAcknowledgedFalseOrderBySeverityDescCreatedAtDesc();
    }
    
    public List<Alert> getAllAlerts() {
        return alertRepository.findAll();
    }
    
    @Transactional
    public Alert acknowledgeAlert(Long alertId) {
        log.info("Acknowledging alert: {}", alertId);
        
        Alert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new AlertNotFoundException("Alert not found: " + alertId));
        
        alert.setAcknowledged(true);
        alert.setAcknowledgedAt(Instant.now());
        alert.setAcknowledgedBy("system");
        
        return alertRepository.save(alert);
    }
    
    private EnvironmentStatus buildEnvironmentStatus(EnvironmentalSettings settings, EnvironmentalReading reading) {
        String status = EnvironmentStatus.calculateStatus(
                reading.getO2Level(), settings.getTargetO2(),
                reading.getTemperature(), settings.getTargetTemperature(),
                reading.getPressure(), settings.getTargetPressure()
        );
        
        return new EnvironmentStatus(
                settings.getSectionId(),
                settings.getSectionName(),
                reading.getO2Level(),
                reading.getCo2Level(),
                reading.getTemperature(),
                reading.getPressure(),
                reading.getHumidity(),
                settings.getTargetO2(),
                settings.getTargetTemperature(),
                settings.getTargetPressure(),
                settings.getTargetHumidity(),
                settings.getCurrentOccupancy(),
                settings.getMaxOccupancy(),
                status,
                reading.getCreatedAt()
        );
    }
    
    private EnvironmentalReading createDefaultReading(EnvironmentalSettings settings) {
        EnvironmentalReading reading = new EnvironmentalReading();
        reading.setSectionId(settings.getSectionId());
        reading.setSectionName(settings.getSectionName());
        reading.setO2Level(settings.getTargetO2());
        reading.setCo2Level(0.04);
        reading.setTemperature(settings.getTargetTemperature());
        reading.setPressure(settings.getTargetPressure());
        reading.setHumidity(settings.getTargetHumidity());
        return reading;
    }
    
    // Exception classes
    public static class SectionNotFoundException extends RuntimeException {
        public SectionNotFoundException(String message) { super(message); }
    }
    
    public static class AlertNotFoundException extends RuntimeException {
        public AlertNotFoundException(String message) { super(message); }
    }
    
    public static class CapacityExceededException extends RuntimeException {
        public CapacityExceededException(String message) { super(message); }
    }
}
