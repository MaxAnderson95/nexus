package com.nexus.docking.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
public class CrewClient {
    
    private static final Logger log = LoggerFactory.getLogger(CrewClient.class);
    
    private final RestClient restClient;
    
    public CrewClient(
            RestClient.Builder restClientBuilder,
            @Value("${services.crew.url}") String crewServiceUrl) {
        this.restClient = restClientBuilder
                .baseUrl(crewServiceUrl)
                .build();
    }
    
    public ArrivalResponse registerArrival(Long shipId, String shipName, Integer crewCount) {
        log.info("Registering crew arrival: {} crew members from ship '{}' (ID: {})", crewCount, shipName, shipId);
        
        try {
            restClient.post()
                    .uri("/api/crew/arrival")
                    .body(Map.of(
                            "shipId", shipId,
                            "crewCount", crewCount
                    ))
                    .retrieve()
                    .toBodilessEntity();
            
            log.info("Crew arrival registration successful for ship '{}'", shipName);
            return new ArrivalResponse(true, "Crew registered successfully", crewCount);
        } catch (Exception e) {
            log.error("Failed to register crew arrival for ship '{}': {}", shipName, e.getMessage());
            // Don't throw - crew registration failure shouldn't block docking
            return new ArrivalResponse(false, "Failed to register crew: " + e.getMessage(), 0);
        }
    }
    
    public void registerDeparture(String shipName) {
        // Crew departure is tracked implicitly - no dedicated endpoint needed for demo
        log.info("Crew departure noted for ship: {}", shipName);
    }
    
    public record ArrivalResponse(
        boolean success,
        String message,
        int crewRegistered
    ) {}
}
