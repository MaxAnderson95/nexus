package com.nexus.cortex.config;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Filter that adds the X-Trace-Id header to all HTTP responses.
 * This allows the frontend to capture trace IDs for error reporting.
 */
@Component
@Order(1)
public class TraceIdFilter implements Filter {

    public static final String TRACE_ID_HEADER = "X-Trace-Id";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        // Add trace ID header before processing - the OTel instrumentation
        // creates the span context before filters run
        if (response instanceof HttpServletResponse httpResponse) {
            SpanContext spanContext = Span.current().getSpanContext();
            if (spanContext.isValid()) {
                httpResponse.setHeader(TRACE_ID_HEADER, spanContext.getTraceId());
            }
        }

        chain.doFilter(request, response);
    }
}
