package com.nexus.cortex.validation;

import java.util.Map;

public final class RequestValidator {

    private RequestValidator() {}

    public static void validatePositiveId(Long id, String name) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException(name + " must be a positive number");
        }
    }

    public static void validateRequired(Map<String, Object> request, String... fields) {
        if (request == null) {
            throw new IllegalArgumentException("Request body is required");
        }
        for (String field : fields) {
            if (!request.containsKey(field) || request.get(field) == null) {
                throw new IllegalArgumentException("Missing required field: " + field);
            }
        }
    }

    public static void validatePositiveNumber(Object value, String name) {
        if (value instanceof Number num && num.longValue() <= 0) {
            throw new IllegalArgumentException(name + " must be a positive number");
        }
    }

    public static void validateNonNegativeNumber(Object value, String name) {
        if (value instanceof Number num && num.doubleValue() < 0) {
            throw new IllegalArgumentException(name + " cannot be negative");
        }
    }

    public static void validateRange(Object value, String name, double min, double max) {
        if (value instanceof Number num) {
            double val = num.doubleValue();
            if (val < min || val > max) {
                throw new IllegalArgumentException(name + " must be between " + min + " and " + max);
            }
        }
    }

    public static void validateNotBlank(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " cannot be blank");
        }
    }
}
