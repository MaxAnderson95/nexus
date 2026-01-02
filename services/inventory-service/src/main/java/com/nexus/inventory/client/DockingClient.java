package com.nexus.inventory.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

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
    
    public ScheduleDeliveryResponse scheduleDelivery(String supplyName, Integer quantity) {
        log.info("Scheduling delivery for {} units of '{}'", quantity, supplyName);

        try {
            // Generate a ship name for the resupply delivery
            String shipName = "Resupply-" + System.currentTimeMillis() % 10000;
            // Estimated arrival is 24-48 hours from now
            java.time.Instant estimatedArrival = java.time.Instant.now()
                    .plus(java.time.Duration.ofHours(24 + (long)(Math.random() * 24)));

            var response = restClient.post()
                    .uri("/api/docking/schedule-delivery")
                    .body(Map.of(
                            "shipName", shipName,
                            "cargoType", supplyName + " (" + quantity + " units)",
                            "estimatedArrival", estimatedArrival.toString()
                    ))
                    .retrieve()
                    .body(ScheduleDeliveryResponse.class);

            log.info("Delivery scheduled successfully: {}", response);
            return response;
        } catch (Exception e) {
            log.error("Failed to schedule delivery: {}", e.getMessage());
            throw new DockingServiceException("Failed to schedule delivery: " + e.getMessage());
        }
    }
    
    public record ScheduleDeliveryResponse(
        Long deliveryId,
        String status,
        String estimatedArrival,
        String message
    ) {}
    
    public static class DockingServiceException extends RuntimeException {
        public DockingServiceException(String message) {
            super(message);
        }
    }
}
