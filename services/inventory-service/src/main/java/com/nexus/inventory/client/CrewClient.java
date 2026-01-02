package com.nexus.inventory.client;

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

    public List<CrewMember> getAvailableCrew() {
        log.info("Calling {} to fetch available crew members", SERVICE_NAME);

        try {
            var response = restClient.get()
                    .uri("/api/crew/available")
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<CrewMember>>() {});

            log.info("Found {} available crew members", response != null ? response.size() : 0);
            return response;
        } catch (HttpServerErrorException | HttpClientErrorException e) {
            String errorDetail = extractErrorDetail(e.getResponseBodyAsString());
            log.error("Call to {} failed: {}", SERVICE_NAME, errorDetail);
            throw new CrewServiceException(SERVICE_NAME, errorDetail);
        } catch (Exception e) {
            log.error("Call to {} failed: {}", SERVICE_NAME, e.getMessage());
            throw new CrewServiceException(SERVICE_NAME, e.getMessage());
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

    public record CrewMember(
        Long id,
        String name,
        String role,
        String status,
        Long sectionId
    ) {}

    public static class CrewServiceException extends RuntimeException {
        private final String serviceName;

        public CrewServiceException(String serviceName, String message) {
            super(message);
            this.serviceName = serviceName;
        }

        public String getServiceName() {
            return serviceName;
        }
    }
}
