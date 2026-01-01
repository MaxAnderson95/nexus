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
    
    public ArrivalResponse registerArrival(String shipName, Integer crewCount) {
        log.info("Registering crew arrival: {} crew members from ship '{}'", crewCount, shipName);
        
        try {
            var response = restClient.post()
                    .uri("/api/crew/register-arrival")
                    .body(Map.of(
                            "shipName", shipName,
                            "crewCount", crewCount
                    ))
                    .retrieve()
                    .body(ArrivalResponse.class);
            
            log.info("Crew arrival registration successful for ship '{}': {}", shipName, response);
            return response;
        } catch (Exception e) {
            log.error("Failed to register crew arrival for ship '{}': {}", shipName, e.getMessage());
            // Don't throw - crew registration failure shouldn't block docking
            return new ArrivalResponse(false, "Failed to register crew: " + e.getMessage(), 0);
        }
    }
    
    public void registerDeparture(String shipName) {
        log.info("Registering crew departure for ship: {}", shipName);
        
        try {
            restClient.post()
                    .uri("/api/crew/register-departure")
                    .body(Map.of("shipName", shipName))
                    .retrieve()
                    .toBodilessEntity();
            
            log.info("Crew departure registration successful for ship: {}", shipName);
        } catch (Exception e) {
            log.error("Failed to register crew departure for ship '{}': {}", shipName, e.getMessage());
            // Don't throw - departure registration failure shouldn't block undocking
        }
    }
    
    public record ArrivalResponse(
        boolean success,
        String message,
        int crewRegistered
    ) {}
}
