package com.nexus.crew.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

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
    
    public void adjustCapacity(Long sectionId, Integer occupancyChange) {
        log.info("Notifying life support of capacity change: section={}, change={}", 
                sectionId, occupancyChange);
        
        try {
            restClient.post()
                    .uri("/api/life-support/adjust-capacity")
                    .body(Map.of(
                            "sectionId", sectionId,
                            "occupancyChange", occupancyChange
                    ))
                    .retrieve()
                    .toBodilessEntity();
            
            log.info("Life support capacity adjustment successful for section: {}", sectionId);
        } catch (Exception e) {
            log.error("Failed to notify life support of capacity change: {}", e.getMessage());
            throw new LifeSupportAdjustmentException(
                    "Failed to adjust life support capacity: " + e.getMessage());
        }
    }
    
    public static class LifeSupportAdjustmentException extends RuntimeException {
        public LifeSupportAdjustmentException(String message) {
            super(message);
        }
    }
}
