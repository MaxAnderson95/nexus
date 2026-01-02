package com.nexus.crew.client;

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
public class LifeSupportClient {

    private static final Logger log = LoggerFactory.getLogger(LifeSupportClient.class);
    private static final String SERVICE_NAME = "life-support-service";
    // Pattern to extract the "message" field from Spring error responses
    private static final Pattern MESSAGE_FIELD_PATTERN = Pattern.compile("\"message\"\\s*:\\s*\"([^\"]+)\"");
    // Pattern to extract chaos engineering details from the message
    private static final Pattern CHAOS_MESSAGE_PATTERN = Pattern.compile("\\[Chaos Engineering\\]\\s*(.+)");

    private final RestClient restClient;

    public LifeSupportClient(
            RestClient.Builder restClientBuilder,
            @Value("${services.life-support.url}") String lifeSupportServiceUrl) {
        this.restClient = restClientBuilder
                .baseUrl(lifeSupportServiceUrl)
                .build();
    }

    public void adjustCapacity(Long sectionId, Integer occupancyChange) {
        log.info("Calling {} to adjust capacity: section={}, change={}",
                SERVICE_NAME, sectionId, occupancyChange);

        try {
            restClient.post()
                    .uri("/api/v1/life-support/adjust-capacity")
                    .body(Map.of(
                            "sectionId", sectionId,
                            "occupancyChange", occupancyChange
                    ))
                    .retrieve()
                    .toBodilessEntity();

            log.info("Life support capacity adjustment successful for section: {}", sectionId);
        } catch (HttpServerErrorException | HttpClientErrorException e) {
            String errorDetail = extractErrorDetail(e.getResponseBodyAsString());
            log.error("Call to {} failed: {}", SERVICE_NAME, errorDetail);
            throw new LifeSupportAdjustmentException(SERVICE_NAME, errorDetail);
        } catch (Exception e) {
            log.error("Call to {} failed: {}", SERVICE_NAME, e.getMessage());
            throw new LifeSupportAdjustmentException(SERVICE_NAME, e.getMessage());
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

    public static class LifeSupportAdjustmentException extends RuntimeException {
        private final String serviceName;

        public LifeSupportAdjustmentException(String serviceName, String message) {
            super(message);
            this.serviceName = serviceName;
        }

        public String getServiceName() {
            return serviceName;
        }
    }
}
