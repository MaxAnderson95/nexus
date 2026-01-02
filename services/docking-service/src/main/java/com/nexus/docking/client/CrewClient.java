package com.nexus.docking.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class CrewClient {

    private static final Logger log = LoggerFactory.getLogger(CrewClient.class);
    private static final String SERVICE_NAME = "crew-service";
    // Pattern to extract the "message" field from Spring error responses
    private static final Pattern MESSAGE_FIELD_PATTERN = Pattern.compile("\"message\"\\s*:\\s*\"([^\"]+)\"");
    // Pattern to extract chaos engineering details from the message
    private static final Pattern CHAOS_MESSAGE_PATTERN = Pattern.compile("\\[Chaos Engineering\\]\\s*(.+)");

    private final RestClient restClient;

    public CrewClient(
            RestClient.Builder restClientBuilder,
            @Value("${services.crew.url}") String crewServiceUrl) {
        this.restClient = restClientBuilder
                .baseUrl(crewServiceUrl)
                .build();
    }

    public ArrivalResponse registerArrival(Long shipId, String shipName, Integer crewCount) {
        log.info("Calling {} to register crew arrival: {} crew members from ship '{}' (ID: {})",
                SERVICE_NAME, crewCount, shipName, shipId);

        try {
            restClient.post()
                    .uri("/api/v1/crew/arrival")
                    .body(Map.of(
                            "shipId", shipId,
                            "crewCount", crewCount
                    ))
                    .retrieve()
                    .toBodilessEntity();

            log.info("Crew arrival registration successful for ship '{}'", shipName);
            return new ArrivalResponse(true, "Crew registered successfully", crewCount);
        } catch (HttpServerErrorException | HttpClientErrorException e) {
            String errorDetail = extractErrorDetail(e.getResponseBodyAsString());
            log.warn("Call to {} failed for ship '{}': {}", SERVICE_NAME, shipName, errorDetail);
            // Don't throw - crew registration failure shouldn't block docking
            return new ArrivalResponse(false, "Failed to register crew: " + errorDetail, 0);
        } catch (Exception e) {
            log.warn("Call to {} failed for ship '{}': {}", SERVICE_NAME, shipName, e.getMessage());
            // Don't throw - crew registration failure shouldn't block docking
            return new ArrivalResponse(false, "Failed to register crew: " + e.getMessage(), 0);
        }
    }

    public void registerDeparture(String shipName) {
        // Crew departure is tracked implicitly - no dedicated endpoint needed for demo
        log.info("Crew departure noted for ship: {}", shipName);
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

    public record ArrivalResponse(
        boolean success,
        String message,
        int crewRegistered
    ) {}
}
