package com.nexus.cortex.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Component
public class LifeSupportClient {
    
    private static final Logger log = LoggerFactory.getLogger(LifeSupportClient.class);
    
    private final RestClient restClient;
    
    public LifeSupportClient(
            RestClient.Builder restClientBuilder,
            @Value("${services.life-support.url}") String lifeSupportServiceUrl) {
        this.restClient = restClientBuilder
                .baseUrl(lifeSupportServiceUrl)
                .build();
    }
    
    // === Summary for Dashboard ===
    
    public LifeSupportSummary getSummary() {
        log.debug("Fetching life support summary");
        
        try {
            Map<String, Object> summary = restClient.get()
                    .uri("/api/life-support/environment/summary")
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});
            
            if (summary == null) {
                return new LifeSupportSummary(0, 0, 0, 0, 0, 21.0, 22.0);
            }
            
            return new LifeSupportSummary(
                    toInt(summary.get("totalSections")),
                    toInt(summary.get("sectionsNominal")),
                    toInt(summary.get("sectionsWarning")),
                    toInt(summary.get("sectionsCritical")),
                    toInt(summary.get("activeAlerts")),
                    toDouble(summary.get("averageO2Level")),
                    toDouble(summary.get("averageTemperature"))
            );
        } catch (Exception e) {
            log.error("Failed to fetch life support summary: {}", e.getMessage());
            throw e;
        }
    }
    
    private int toInt(Object value) {
        if (value == null) return 0;
        if (value instanceof Integer) return (Integer) value;
        if (value instanceof Number) return ((Number) value).intValue();
        return 0;
    }
    
    private double toDouble(Object value) {
        if (value == null) return 0.0;
        if (value instanceof Double) return (Double) value;
        if (value instanceof Number) return ((Number) value).doubleValue();
        return 0.0;
    }
    
    // === Proxy Methods ===
    
    public List<Map<String, Object>> getAllEnvironment() {
        log.debug("Fetching all environment status");
        return restClient.get()
                .uri("/api/life-support/environment")
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }
    
    public Map<String, Object> getEnvironmentSummary() {
        log.debug("Fetching environment summary");
        return restClient.get()
                .uri("/api/life-support/environment/summary")
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }
    
    public Map<String, Object> getSectionEnvironment(Long sectionId) {
        log.debug("Fetching section environment: {}", sectionId);
        return restClient.get()
                .uri("/api/life-support/environment/section/{sectionId}", sectionId)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }
    
    public Map<String, Object> adjustEnvironment(Long sectionId, Map<String, Object> request) {
        log.info("Adjusting environment for section {}: {}", sectionId, request);
        return restClient.post()
                .uri("/api/life-support/environment/section/{sectionId}/adjust", sectionId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }
    
    public Map<String, Object> adjustCapacity(Map<String, Object> request) {
        log.info("Adjusting life support capacity: {}", request);
        return restClient.post()
                .uri("/api/life-support/adjust-capacity")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }
    
    public List<Map<String, Object>> getAlerts() {
        log.debug("Fetching active alerts");
        return restClient.get()
                .uri("/api/life-support/alerts")
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }
    
    public List<Map<String, Object>> getAllAlerts() {
        log.debug("Fetching all alerts");
        return restClient.get()
                .uri("/api/life-support/alerts/all")
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }
    
    public Map<String, Object> acknowledgeAlert(Long alertId) {
        log.info("Acknowledging alert: {}", alertId);
        return restClient.post()
                .uri("/api/life-support/alerts/{alertId}/acknowledge", alertId)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }
    
    public record LifeSupportSummary(
        int totalSections,
        int sectionsNominal,
        int sectionsWarning,
        int sectionsCritical,
        int activeAlerts,
        double averageO2Level,
        double averageTemperature
    ) {}
}
