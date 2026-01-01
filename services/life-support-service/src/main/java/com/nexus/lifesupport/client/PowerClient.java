package com.nexus.lifesupport.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

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
    
    public AllocationResponse allocatePower(String system, Double amountKw, Long sectionId) {
        log.info("Requesting power allocation: {} kW for system '{}' in section {}", 
                amountKw, system, sectionId);
        
        try {
            var response = restClient.post()
                    .uri("/api/power/allocate")
                    .body(Map.of(
                            "system", system,
                            "amountKw", amountKw,
                            "sectionId", sectionId,
                            "priority", 1  // Life support is highest priority
                    ))
                    .retrieve()
                    .body(AllocationResponse.class);
            
            log.info("Power allocation successful: {}", response);
            return response;
        } catch (Exception e) {
            log.error("Failed to allocate power: {}", e.getMessage());
            throw new PowerAllocationException("Failed to allocate power for life support: " + e.getMessage());
        }
    }
    
    public void deallocatePower(String system) {
        log.info("Deallocating power for system: {}", system);
        
        try {
            restClient.post()
                    .uri("/api/power/deallocate")
                    .body(Map.of("system", system))
                    .retrieve()
                    .toBodilessEntity();
            
            log.info("Power deallocation successful for system: {}", system);
        } catch (Exception e) {
            log.error("Failed to deallocate power: {}", e.getMessage());
            // Don't throw - deallocation failure shouldn't block operations
        }
    }
    
    public record AllocationResponse(
        Long id,
        String systemName,
        Double allocatedKw,
        Integer priority,
        Long sectionId,
        String message
    ) {}
    
    public static class PowerAllocationException extends RuntimeException {
        public PowerAllocationException(String message) {
            super(message);
        }
    }
}
