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
public class PowerClient {
    
    private static final Logger log = LoggerFactory.getLogger(PowerClient.class);
    
    private final RestClient restClient;
    
    public PowerClient(
            RestClient.Builder restClientBuilder,
            @Value("${services.power.url}") String powerServiceUrl) {
        this.restClient = restClientBuilder
                .baseUrl(powerServiceUrl)
                .build();
    }
    
    // === Summary for Dashboard ===
    
    public PowerSummary getSummary() {
        log.debug("Fetching power summary");
        
        try {
            Map<String, Object> grid = restClient.get()
                    .uri("/api/power/grid")
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});
            
            if (grid == null) {
                return new PowerSummary(0.0, 0.0, 0.0, 0.0, 0.0, 0, 0);
            }
            
            return new PowerSummary(
                    toDouble(grid.get("totalCapacityKw")),
                    toDouble(grid.get("totalOutputKw")),
                    toDouble(grid.get("totalAllocatedKw")),
                    toDouble(grid.get("availableKw")),
                    toDouble(grid.get("utilizationPercent")),
                    toInt(grid.get("onlineSources")),
                    toInt(grid.get("totalSources"))
            );
        } catch (Exception e) {
            log.error("Failed to fetch power summary: {}", e.getMessage());
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
    
    public Map<String, Object> getPowerStatus() {
        log.debug("Fetching power status");
        return restClient.get()
                .uri("/api/power")
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }
    
    public Map<String, Object> getGridStatus() {
        log.debug("Fetching grid status");
        return restClient.get()
                .uri("/api/power/grid")
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }
    
    public List<Map<String, Object>> getAllSources() {
        log.debug("Fetching all power sources");
        return restClient.get()
                .uri("/api/power/sources")
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }
    
    public Map<String, Object> getSourceById(Long id) {
        log.debug("Fetching power source: {}", id);
        return restClient.get()
                .uri("/api/power/sources/{id}", id)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }
    
    public Map<String, Object> allocatePower(Map<String, Object> request) {
        log.info("Allocating power: {}", request);
        return restClient.post()
                .uri("/api/power/allocate")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }
    
    public Map<String, Object> deallocatePower(Map<String, Object> request) {
        log.info("Deallocating power: {}", request);
        return restClient.post()
                .uri("/api/power/deallocate")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }
    
    public List<Map<String, Object>> getAllAllocations() {
        log.debug("Fetching all power allocations");
        return restClient.get()
                .uri("/api/power/allocations")
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }
    
    public Map<String, Object> getAllocationBySystem(String system) {
        log.debug("Fetching power allocation for system: {}", system);
        return restClient.get()
                .uri("/api/power/allocation/{system}", system)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }
    
    public record PowerSummary(
        double totalCapacityKw,
        double totalOutputKw,
        double totalAllocatedKw,
        double availableKw,
        double utilizationPercent,
        int onlineSources,
        int totalSources
    ) {}
}
