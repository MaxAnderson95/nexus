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
public class PowerClient {

    private static final Logger log = LoggerFactory.getLogger(PowerClient.class);
    private static final String SERVICE_NAME = "power-service";
    // Pattern to extract the "message" field from Spring error responses
    private static final Pattern MESSAGE_FIELD_PATTERN = Pattern.compile("\"message\"\\s*:\\s*\"([^\"]+)\"");
    // Pattern to extract chaos engineering details from the message
    private static final Pattern CHAOS_MESSAGE_PATTERN = Pattern.compile("\\[Chaos Engineering\\]\\s*(.+)");

    private final RestClient restClient;

    public PowerClient(
            RestClient.Builder restClientBuilder,
            @Value("${services.power.url}") String powerServiceUrl) {
        this.restClient = restClientBuilder
                .baseUrl(powerServiceUrl)
                .build();
    }

    public AllocationResponse allocatePowerForBay(Long bayId, Double amountKw) {
        String system = "docking_bay_" + bayId;
        log.info("Calling {} to allocate {} kW for docking bay {}", SERVICE_NAME, amountKw, bayId);

        try {
            var response = restClient.post()
                    .uri("/api/power/allocate")
                    .body(Map.of(
                            "system", system,
                            "amountKw", amountKw,
                            "priority", 4  // Docking systems priority
                    ))
                    .retrieve()
                    .body(AllocationResponse.class);

            log.info("Power allocation successful for docking bay {}: {}", bayId, response);
            return response;
        } catch (HttpServerErrorException | HttpClientErrorException e) {
            String errorDetail = extractErrorDetail(e.getResponseBodyAsString());
            log.error("Call to {} failed for docking bay {}: {}", SERVICE_NAME, bayId, errorDetail);
            throw new PowerAllocationException(SERVICE_NAME, errorDetail);
        } catch (Exception e) {
            log.error("Call to {} failed for docking bay {}: {}", SERVICE_NAME, bayId, e.getMessage());
            throw new PowerAllocationException(SERVICE_NAME, e.getMessage());
        }
    }

    public void deallocatePowerForBay(Long bayId) {
        String system = "docking_bay_" + bayId;
        log.info("Calling {} to deallocate power for docking bay {}", SERVICE_NAME, bayId);

        try {
            restClient.post()
                    .uri("/api/power/deallocate")
                    .body(Map.of("system", system))
                    .retrieve()
                    .toBodilessEntity();

            log.info("Power deallocation successful for docking bay: {}", bayId);
        } catch (HttpServerErrorException | HttpClientErrorException e) {
            String errorDetail = extractErrorDetail(e.getResponseBodyAsString());
            log.warn("Call to {} failed during deallocation for bay {}: {}", SERVICE_NAME, bayId, errorDetail);
            // Don't throw - deallocation failure shouldn't block operations
        } catch (Exception e) {
            log.warn("Call to {} failed during deallocation for bay {}: {}", SERVICE_NAME, bayId, e.getMessage());
            // Don't throw - deallocation failure shouldn't block operations
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

    public record AllocationResponse(
        Long id,
        String systemName,
        Double allocatedKw,
        Integer priority,
        Long sectionId,
        String message
    ) {}

    public static class PowerAllocationException extends RuntimeException {
        private final String serviceName;

        public PowerAllocationException(String serviceName, String message) {
            super(message);
            this.serviceName = serviceName;
        }

        public String getServiceName() {
            return serviceName;
        }
    }
}
