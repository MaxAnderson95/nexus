package com.nexus.docking.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class InventoryClient {

    private static final Logger log = LoggerFactory.getLogger(InventoryClient.class);
    private static final String SERVICE_NAME = "inventory-service";
    // Pattern to extract the "message" field from Spring error responses
    private static final Pattern MESSAGE_FIELD_PATTERN = Pattern.compile("\"message\"\\s*:\\s*\"([^\"]+)\"");
    // Pattern to extract chaos engineering details from the message
    private static final Pattern CHAOS_MESSAGE_PATTERN = Pattern.compile("\\[Chaos Engineering\\]\\s*(.+)");

    private final RestClient restClient;

    public InventoryClient(
            RestClient.Builder restClientBuilder,
            @Value("${services.inventory.url}") String inventoryServiceUrl) {
        this.restClient = restClientBuilder
                .baseUrl(inventoryServiceUrl)
                .build();
    }

    public List<UnloadedManifest> unloadCargoForShip(Long shipId) {
        log.info("Calling {} to unload cargo manifests for ship ID: {}", SERVICE_NAME, shipId);

        try {
            var response = restClient.post()
                    .uri("/api/v1/inventory/cargo-manifests/unload-by-ship/{shipId}", shipId)
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<UnloadedManifest>>() {});

            log.info("Cargo unload successful for ship ID {}: {} manifests processed", shipId,
                    response != null ? response.size() : 0);
            return response != null ? response : List.of();
        } catch (HttpServerErrorException | HttpClientErrorException e) {
            String errorDetail = extractErrorDetail(e.getResponseBodyAsString());
            log.error("Call to {} failed for ship ID {}: {}", SERVICE_NAME, shipId, errorDetail);
            throw new InventoryServiceException(SERVICE_NAME, errorDetail);
        } catch (Exception e) {
            log.error("Call to {} failed for ship ID {}: {}", SERVICE_NAME, shipId, e.getMessage());
            throw new InventoryServiceException(SERVICE_NAME, e.getMessage());
        }
    }

    private String extractErrorDetail(String responseBody) {
        if (responseBody == null || responseBody.isEmpty()) {
            return "Unknown error";
        }

        // First extract the message field from the JSON response
        Matcher messageMatcher = MESSAGE_FIELD_PATTERN.matcher(responseBody);
        if (messageMatcher.find()) {
            String message = messageMatcher.group(1);
            // Check if it's a chaos engineering message and extract the actual error
            Matcher chaosMatcher = CHAOS_MESSAGE_PATTERN.matcher(message);
            if (chaosMatcher.find()) {
                return "[Chaos Engineering] " + chaosMatcher.group(1).trim();
            }
            return message;
        }

        // Return truncated body as fallback
        return responseBody.length() > 200 ? responseBody.substring(0, 200) + "..." : responseBody;
    }

    public record UnloadedManifest(
        Long id,
        Long shipId,
        String shipName,
        String status,
        List<CargoItem> items
    ) {}

    public record CargoItem(
        Long id,
        Long supplyId,
        String supplyName,
        Integer quantity
    ) {}

    public static class InventoryServiceException extends RuntimeException {
        private final String serviceName;

        public InventoryServiceException(String serviceName, String message) {
            super(message);
            this.serviceName = serviceName;
        }

        public String getServiceName() {
            return serviceName;
        }
    }
}
