package com.nexus.cortex.exception;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Global exception handler for Cortex (public-facing BFF).
 *
 * Security policy:
 * - 4xx client errors: Messages are passed through since they contain user-actionable
 *   business logic information (e.g., "No docking bay available", "Section at capacity")
 * - 5xx server errors: Messages are sanitized - only generic messages and trace IDs are
 *   returned. Operators can use the trace ID to look up full details in the tracing system.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private String getCurrentTraceId() {
        SpanContext spanContext = Span.current().getSpanContext();
        if (spanContext.isValid()) {
            return spanContext.getTraceId();
        }
        return null;
    }

    /**
     * Build an error response for public consumption.
     * The message parameter should be user-safe (sanitized for 5xx, business logic for 4xx).
     */
    private Map<String, Object> buildPublicErrorResponse(int status, String error, String message) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("timestamp", Instant.now().toString());
        response.put("status", status);
        response.put("error", error);
        response.put("message", message);

        String traceId = getCurrentTraceId();
        if (traceId != null) {
            response.put("traceId", traceId);
        }

        return response;
    }

    /**
     * Extract error details from downstream service response body for logging.
     */
    private DownstreamError extractDownstreamError(HttpStatusCodeException ex) {
        String responseBody = ex.getResponseBodyAsString();
        if (responseBody == null || responseBody.isEmpty()) {
            return new DownstreamError(null, null, null);
        }

        try {
            JsonNode root = objectMapper.readTree(responseBody);

            String message = getJsonString(root, "message");
            String downstreamService = getJsonString(root, "downstreamService");
            String innerError = getJsonString(root, "innerError");

            if (message == null) {
                message = getJsonString(root, "error");
            }

            return new DownstreamError(message, downstreamService, innerError);
        } catch (Exception e) {
            return new DownstreamError(
                    responseBody.length() > 200 ? responseBody.substring(0, 200) : responseBody,
                    null, null);
        }
    }

    private String getJsonString(JsonNode root, String field) {
        JsonNode node = root.get(field);
        return node != null && !node.isNull() ? node.asText() : null;
    }

    private record DownstreamError(String message, String downstreamService, String innerError) {}

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Bad request: {}", ex.getMessage());
        return ResponseEntity.badRequest()
                .body(buildPublicErrorResponse(400, "Bad Request", "Invalid request parameters"));
    }

    @ExceptionHandler(HttpClientErrorException.class)
    public ResponseEntity<Map<String, Object>> handleHttpClientError(HttpClientErrorException ex) {
        DownstreamError error = extractDownstreamError(ex);
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());

        // Log full details internally
        if (error.downstreamService() != null) {
            log.warn("Downstream error from {}: {} (cause: {})",
                    error.downstreamService(), error.message(), error.innerError());
        } else {
            log.warn("Downstream client error {}: {}", status, error.message());
        }

        // 4xx errors are typically user-actionable business logic errors (e.g., "No docking bay available")
        // Pass through the actual error message so users understand what went wrong
        String clientMessage = error.message() != null ? error.message() : "Request could not be processed";
        return ResponseEntity.status(status)
                .body(buildPublicErrorResponse(status.value(), status.getReasonPhrase(), clientMessage));
    }

    @ExceptionHandler(HttpServerErrorException.class)
    public ResponseEntity<Map<String, Object>> handleHttpServerError(HttpServerErrorException ex) {
        DownstreamError error = extractDownstreamError(ex);

        // Log full details internally
        if (error.downstreamService() != null) {
            log.error("Downstream error from {}: {} (cause: {})",
                    error.downstreamService(), error.message(), error.innerError());
        } else {
            log.error("Downstream server error {}: {}", ex.getStatusCode(), error.message());
        }

        // Return sanitized response to client
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(buildPublicErrorResponse(502, "Bad Gateway",
                        "Service temporarily unavailable"));
    }

    @ExceptionHandler(RestClientException.class)
    public ResponseEntity<Map<String, Object>> handleRestClientException(RestClientException ex) {
        log.error("REST client error: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(buildPublicErrorResponse(502, "Bad Gateway",
                        "Service temporarily unavailable"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(buildPublicErrorResponse(500, "Internal Server Error",
                        "An unexpected error occurred"));
    }
}
