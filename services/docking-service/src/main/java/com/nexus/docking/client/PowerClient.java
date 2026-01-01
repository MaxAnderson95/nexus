package com.nexus.docking.client;

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
    
    public AllocationResponse allocatePowerForBay(Long bayId, Double amountKw) {
        String system = "docking_bay_" + bayId;
        log.info("Requesting power allocation: {} kW for docking bay {}", amountKw, bayId);
        
        try {
            var response = restClient.post()
                    .uri("/api/power/allocate")
                    .body(Map.of(
                            "system", system,
                            "amountKw", amountKw,
                            "priority", 4  // Docking systems priority
                    ))
                    .retrieve()
                    .body(AllocationResponse.class);
            
            log.info("Power allocation successful for docking bay {}: {}", bayId, response);
            return response;
        } catch (Exception e) {
            log.error("Failed to allocate power for docking bay {}: {}", bayId, e.getMessage());
            throw new PowerAllocationException("Failed to allocate power for docking bay: " + e.getMessage());
        }
    }
    
    public void deallocatePowerForBay(Long bayId) {
        String system = "docking_bay_" + bayId;
        log.info("Deallocating power for docking bay: {}", bayId);
        
        try {
            restClient.post()
                    .uri("/api/power/deallocate")
                    .body(Map.of("system", system))
                    .retrieve()
                    .toBodilessEntity();
            
            log.info("Power deallocation successful for docking bay: {}", bayId);
        } catch (Exception e) {
            log.error("Failed to deallocate power for docking bay {}: {}", bayId, e.getMessage());
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
