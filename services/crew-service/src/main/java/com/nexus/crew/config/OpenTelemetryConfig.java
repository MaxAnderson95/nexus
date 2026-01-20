package com.nexus.crew.config;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Tracer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenTelemetryConfig {
    
    @Bean
    public Tracer tracer() {
        // Use GlobalOpenTelemetry which is set by the Java agent (injected by Dash0 operator)
        return GlobalOpenTelemetry.get().getTracer("com.nexus.crew");
    }
}
