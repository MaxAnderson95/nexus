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
    
    // === Summary for Dashboard ===
    
    public CrewSummary getSummary() {
        log.debug("Fetching crew summary");
        
        try {
            Map<String, Object> summary = restClient.get()
                    .uri("/api/v1/crew/count")
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});
            
            if (summary == null) {
                return new CrewSummary(0L, 0L, 0L, 0L, 0L);
            }
            
            return new CrewSummary(
                    toLong(summary.get("totalCrew")),
                    toLong(summary.get("activeCrew")),
                    toLong(summary.get("onLeaveCrew")),
                    toLong(summary.get("offDutyCrew")),
                    toLong(summary.get("inTransitCrew"))
            );
        } catch (Exception e) {
            log.error("Failed to fetch crew summary: {}", e.getMessage());
            throw e;
        }
    }
    
    private Long toLong(Object value) {
        if (value == null) return 0L;
        if (value instanceof Long) return (Long) value;
        if (value instanceof Integer) return ((Integer) value).longValue();
        if (value instanceof Number) return ((Number) value).longValue();
        return 0L;
    }
    
    // === Proxy Methods ===
    
    public List<Map<String, Object>> getAllCrew() {
        log.debug("Fetching all crew");
        return restClient.get()
                .uri("/api/v1/crew")
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }
    
    public Map<String, Object> getCrewCount() {
        log.debug("Fetching crew count");
        return restClient.get()
                .uri("/api/v1/crew/count")
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }
    
    public Map<String, Object> getCrewById(Long id) {
        log.debug("Fetching crew member: {}", id);
        return restClient.get()
                .uri("/api/v1/crew/{id}", id)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }
    
    public List<Map<String, Object>> getCrewBySection(Long sectionId) {
        log.debug("Fetching crew by section: {}", sectionId);
        return restClient.get()
                .uri("/api/v1/crew/section/{sectionId}", sectionId)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }
    
    public List<Map<String, Object>> getAvailableCrew() {
        log.debug("Fetching available crew");
        return restClient.get()
                .uri("/api/v1/crew/available")
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }
    
    public Map<String, Object> relocateCrew(Map<String, Object> request) {
        log.info("Relocating crew: {}", request);
        return restClient.post()
                .uri("/api/v1/crew/relocate")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }
    
    public List<Map<String, Object>> registerArrival(Map<String, Object> request) {
        log.info("Registering crew arrival: {}", request);
        return restClient.post()
                .uri("/api/v1/crew/arrival")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }
    
    public List<Map<String, Object>> getAllSections() {
        log.debug("Fetching all sections");
        return restClient.get()
                .uri("/api/v1/crew/sections")
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }
    
    public Map<String, Object> getSectionById(Long id) {
        log.debug("Fetching section: {}", id);
        return restClient.get()
                .uri("/api/v1/crew/sections/{id}", id)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }
    
    public Map<String, Object> getSectionHeadcount(Long id) {
        log.debug("Fetching section headcount: {}", id);
        return restClient.get()
                .uri("/api/v1/crew/sections/{id}/headcount", id)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }
    
    public record CrewSummary(
        Long totalCrew,
        Long activeCrew,
        Long onLeaveCrew,
        Long offDutyCrew,
        Long inTransitCrew
    ) {}
}
