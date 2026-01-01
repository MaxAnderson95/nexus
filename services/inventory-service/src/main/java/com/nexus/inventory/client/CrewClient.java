package com.nexus.inventory.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

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
    
    public List<CrewMember> getAvailableCrew() {
        log.info("Fetching available crew members");
        
        try {
            var response = restClient.get()
                    .uri("/api/crew/available")
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<CrewMember>>() {});
            
            log.info("Found {} available crew members", response != null ? response.size() : 0);
            return response;
        } catch (Exception e) {
            log.error("Failed to fetch available crew: {}", e.getMessage());
            throw new CrewServiceException("Failed to fetch available crew: " + e.getMessage());
        }
    }
    
    public record CrewMember(
        Long id,
        String name,
        String role,
        String status,
        Long sectionId
    ) {}
    
    public static class CrewServiceException extends RuntimeException {
        public CrewServiceException(String message) {
            super(message);
        }
    }
}
