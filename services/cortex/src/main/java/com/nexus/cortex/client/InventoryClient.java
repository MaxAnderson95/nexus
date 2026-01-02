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
public class InventoryClient {
    
    private static final Logger log = LoggerFactory.getLogger(InventoryClient.class);
    
    private final RestClient restClient;
    
    public InventoryClient(
            RestClient.Builder restClientBuilder,
            @Value("${services.inventory.url}") String inventoryServiceUrl) {
        this.restClient = restClientBuilder
                .baseUrl(inventoryServiceUrl)
                .build();
    }
    
    // === Summary for Dashboard ===
    
    public InventorySummary getSummary() {
        log.debug("Fetching inventory summary");
        
        try {
            List<Map<String, Object>> supplies = restClient.get()
                    .uri("/api/inventory/supplies")
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});
            
            List<Map<String, Object>> resupplyRequests = restClient.get()
                    .uri("/api/inventory/resupply-requests")
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});
            
            List<Map<String, Object>> manifests = restClient.get()
                    .uri("/api/inventory/cargo-manifests")
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});
            
            int totalItems = supplies != null ? supplies.size() : 0;
            int lowStockItems = 0;
            if (supplies != null) {
                for (Map<String, Object> supply : supplies) {
                    Boolean isLowStock = (Boolean) supply.get("lowStock");
                    if (Boolean.TRUE.equals(isLowStock)) {
                        lowStockItems++;
                    }
                }
            }
            
            int pendingResupply = 0;
            if (resupplyRequests != null) {
                for (Map<String, Object> request : resupplyRequests) {
                    String status = (String) request.get("status");
                    if ("PENDING".equals(status) || "IN_TRANSIT".equals(status)) {
                        pendingResupply++;
                    }
                }
            }
            
            int pendingManifests = 0;
            if (manifests != null) {
                for (Map<String, Object> manifest : manifests) {
                    String status = (String) manifest.get("status");
                    if ("PENDING".equals(status) || "UNLOADING".equals(status)) {
                        pendingManifests++;
                    }
                }
            }
            
            return new InventorySummary(totalItems, lowStockItems, pendingResupply, pendingManifests);
        } catch (Exception e) {
            log.error("Failed to fetch inventory summary: {}", e.getMessage());
            throw e;
        }
    }
    
    private long toLong(Object value) {
        if (value == null) return 0L;
        if (value instanceof Long) return (Long) value;
        if (value instanceof Integer) return ((Integer) value).longValue();
        if (value instanceof Number) return ((Number) value).longValue();
        return 0L;
    }
    
    // === Proxy Methods ===
    
    public Map<String, Object> getInventoryStatus() {
        log.debug("Fetching inventory status");
        return restClient.get()
                .uri("/api/inventory")
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }
    
    public List<Map<String, Object>> getAllSupplies() {
        log.debug("Fetching all supplies");
        return restClient.get()
                .uri("/api/inventory/supplies")
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }
    
    public Map<String, Object> getSupplyById(Long id) {
        log.debug("Fetching supply: {}", id);
        return restClient.get()
                .uri("/api/inventory/supplies/{id}", id)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }
    
    public List<Map<String, Object>> getLowStockSupplies() {
        log.debug("Fetching low stock supplies");
        return restClient.get()
                .uri("/api/inventory/supplies/low-stock")
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }
    
    public Map<String, Object> getLowStockCount() {
        log.debug("Fetching low stock count");
        return restClient.get()
                .uri("/api/inventory/supplies/low-stock/count")
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }
    
    public Map<String, Object> consumeSupply(Map<String, Object> request) {
        log.info("Consuming supply: {}", request);
        return restClient.post()
                .uri("/api/inventory/consume")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }
    
    public Map<String, Object> requestResupply(Map<String, Object> request) {
        log.info("Requesting resupply: {}", request);
        return restClient.post()
                .uri("/api/inventory/resupply")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }
    
    public List<Map<String, Object>> getResupplyRequests() {
        log.debug("Fetching resupply requests");
        return restClient.get()
                .uri("/api/inventory/resupply-requests")
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }
    
    public List<Map<String, Object>> getCargoManifests() {
        log.debug("Fetching cargo manifests");
        return restClient.get()
                .uri("/api/inventory/cargo-manifests")
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }
    
    public Map<String, Object> getCargoManifestById(Long id) {
        log.debug("Fetching cargo manifest: {}", id);
        return restClient.get()
                .uri("/api/inventory/cargo-manifests/{id}", id)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }
    
    public Map<String, Object> unloadManifest(Long id) {
        log.info("Unloading manifest: {}", id);
        return restClient.post()
                .uri("/api/inventory/cargo-manifests/{id}/unload", id)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }
    
    public record InventorySummary(
        int totalItems,
        int lowStockItems,
        int pendingResupply,
        int pendingManifests
    ) {}
}
