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
public class DockingClient {
    
    private static final Logger log = LoggerFactory.getLogger(DockingClient.class);
    
    private final RestClient restClient;
    
    public DockingClient(
            RestClient.Builder restClientBuilder,
            @Value("${services.docking.url}") String dockingServiceUrl) {
        this.restClient = restClientBuilder
                .baseUrl(dockingServiceUrl)
                .build();
    }
    
    // === Summary for Dashboard ===
    
    public DockingSummary getSummary() {
        log.debug("Fetching docking summary");
        
        try {
            List<Map<String, Object>> bays = restClient.get()
                    .uri("/api/docking/bays")
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});
            
            List<Map<String, Object>> incomingShips = restClient.get()
                    .uri("/api/docking/ships/incoming")
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});
            
            int available = 0;
            int occupied = 0;
            int reserved = 0;
            
            if (bays != null) {
                for (Map<String, Object> bay : bays) {
                    String status = (String) bay.get("status");
                    if ("AVAILABLE".equals(status)) available++;
                    else if ("OCCUPIED".equals(status)) occupied++;
                    else if ("RESERVED".equals(status)) reserved++;
                }
            }
            
            int incoming = incomingShips != null ? incomingShips.size() : 0;
            int totalBays = bays != null ? bays.size() : 0;
            
            return new DockingSummary(totalBays, available, occupied, reserved, incoming, occupied);
        } catch (Exception e) {
            log.error("Failed to fetch docking summary: {}", e.getMessage());
            throw e;
        }
    }
    
    // === Proxy Methods ===
    
    public List<Map<String, Object>> getAllBays() {
        log.debug("Fetching all docking bays");
        return restClient.get()
                .uri("/api/docking/bays")
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }
    
    public Map<String, Object> getBayById(Long id) {
        log.debug("Fetching docking bay: {}", id);
        return restClient.get()
                .uri("/api/docking/bays/{id}", id)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }
    
    public List<Map<String, Object>> getAllShips() {
        log.debug("Fetching all ships");
        return restClient.get()
                .uri("/api/docking/ships")
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }
    
    public Map<String, Object> getShipById(Long id) {
        log.debug("Fetching ship: {}", id);
        return restClient.get()
                .uri("/api/docking/ships/{id}", id)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }
    
    public List<Map<String, Object>> getIncomingShips() {
        log.debug("Fetching incoming ships");
        return restClient.get()
                .uri("/api/docking/ships/incoming")
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }
    
    public Map<String, Object> dockShip(Long shipId) {
        log.info("Docking ship: {}", shipId);
        return restClient.post()
                .uri("/api/docking/dock/{shipId}", shipId)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }
    
    public Map<String, Object> undockShip(Long shipId) {
        log.info("Undocking ship: {}", shipId);
        return restClient.post()
                .uri("/api/docking/undock/{shipId}", shipId)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }
    
    public List<Map<String, Object>> getDockingLogs() {
        log.debug("Fetching docking logs");
        return restClient.get()
                .uri("/api/docking/logs")
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }
    
    public Map<String, Object> scheduleDelivery(Map<String, Object> request) {
        log.info("Scheduling delivery: {}", request);
        return restClient.post()
                .uri("/api/docking/schedule-delivery")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }
    
    public record DockingSummary(
        int totalBays,
        int availableBays,
        int occupiedBays,
        int reservedBays,
        int incomingShips,
        int dockedShips
    ) {}
}
