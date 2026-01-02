package com.nexus.cortex.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    
    private static final Logger log = LoggerFactory.getLogger(AdminController.class);
    
    private final RestClient restClient;
    private final String powerServiceUrl;
    private final String lifeSupportServiceUrl;
    private final String crewServiceUrl;
    private final String dockingServiceUrl;
    private final String inventoryServiceUrl;
    private final ExecutorService executor = Executors.newFixedThreadPool(5);
    
    public AdminController(
            RestClient.Builder restClientBuilder,
            @Value("${services.power.url}") String powerServiceUrl,
            @Value("${services.life-support.url}") String lifeSupportServiceUrl,
            @Value("${services.crew.url}") String crewServiceUrl,
            @Value("${services.docking.url}") String dockingServiceUrl,
            @Value("${services.inventory.url}") String inventoryServiceUrl) {
        this.restClient = restClientBuilder.build();
        this.powerServiceUrl = powerServiceUrl;
        this.lifeSupportServiceUrl = lifeSupportServiceUrl;
        this.crewServiceUrl = crewServiceUrl;
        this.dockingServiceUrl = dockingServiceUrl;
        this.inventoryServiceUrl = inventoryServiceUrl;
    }
    
    @PostMapping("/resetAllTables")
    public ResponseEntity<Map<String, Object>> resetAllTables() {
        log.info("Admin: Resetting tables for all services");
        
        List<Map<String, Object>> results = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        
        // Reset services in dependency order (power first, then others)
        // Power has no dependencies
        // Life Support depends on Power
        // Crew depends on Life Support
        // Docking depends on Power, Crew
        // Inventory depends on Docking, Crew
        
        // We'll reset in reverse dependency order to be safe, but since we're
        // clearing all data it doesn't matter much. We'll do them in parallel for speed.
        
        List<CompletableFuture<Map<String, Object>>> futures = new ArrayList<>();
        
        futures.add(resetServiceAsync("power", powerServiceUrl));
        futures.add(resetServiceAsync("life-support", lifeSupportServiceUrl));
        futures.add(resetServiceAsync("crew", crewServiceUrl));
        futures.add(resetServiceAsync("docking", dockingServiceUrl));
        futures.add(resetServiceAsync("inventory", inventoryServiceUrl));
        
        // Wait for all to complete
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        
        for (CompletableFuture<Map<String, Object>> future : futures) {
            try {
                Map<String, Object> result = future.get();
                results.add(result);
                if ("error".equals(result.get("status"))) {
                    errors.add((String) result.get("service"));
                }
            } catch (Exception e) {
                log.error("Failed to get result from future", e);
            }
        }
        
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", errors.isEmpty() ? "success" : "partial");
        response.put("message", errors.isEmpty() 
            ? "All services reset successfully" 
            : "Some services failed to reset: " + String.join(", ", errors));
        response.put("results", results);
        
        return ResponseEntity.ok(response);
    }
    
    private CompletableFuture<Map<String, Object>> resetServiceAsync(String serviceName, String serviceUrl) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.info("Resetting tables for {} at {}", serviceName, serviceUrl);
                Map<String, Object> result = restClient.post()
                        .uri(serviceUrl + "/api/admin/resetTables")
                        .retrieve()
                        .body(new ParameterizedTypeReference<>() {});
                
                if (result == null) {
                    return Map.of(
                        "service", serviceName,
                        "status", "error",
                        "message", "No response from service"
                    );
                }
                return result;
            } catch (Exception e) {
                log.error("Failed to reset tables for {}: {}", serviceName, e.getMessage());
                return Map.of(
                    "service", serviceName,
                    "status", "error",
                    "message", e.getMessage()
                );
            }
        }, executor);
    }
}
