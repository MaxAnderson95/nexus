package com.nexus.lifesupport.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Map;
import java.util.Random;

@Configuration
public class ChaosConfig implements WebMvcConfigurer {
    
    private static final Logger log = LoggerFactory.getLogger(ChaosConfig.class);
    
    @Value("${nexus.chaos.level:none}")
    private String chaosLevel;
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new ChaosInterceptor(chaosLevel))
                .addPathPatterns("/api/**")
                .excludePathPatterns("/actuator/**");
    }
    
    public static class ChaosInterceptor implements HandlerInterceptor {
        
        private final String chaosLevel;
        private final Random random = new Random();
        
        private record ChaosSettings(double errorRate, int minDelayMs, int maxDelayMs) {}
        
        private static final Map<String, ChaosSettings> CHAOS_CONFIGS = Map.of(
                "none", new ChaosSettings(0.0, 0, 0),
                "low", new ChaosSettings(0.05, 200, 500),
                "medium", new ChaosSettings(0.15, 1000, 3000),
                "high", new ChaosSettings(0.30, 3000, 8000)
        );
        
        private static final String[] ERROR_MESSAGES = {
                "Atmospheric processor malfunction",
                "O2 scrubber offline",
                "Temperature regulation failure",
                "Pressure seal breach detected",
                "Humidity control system error",
                "Air recycler fault",
                "Environmental sensor failure"
        };
        
        public ChaosInterceptor(String chaosLevel) {
            this.chaosLevel = chaosLevel != null ? chaosLevel.toLowerCase() : "none";
        }
        
        @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
            String level = chaosLevel.toLowerCase();
            ChaosSettings config = CHAOS_CONFIGS.getOrDefault(level, 
                    "none".equals(level) ? CHAOS_CONFIGS.get("none") : CHAOS_CONFIGS.get("medium"));
            
            if (config.maxDelayMs() > 0) {
                int delay = config.minDelayMs() + random.nextInt(config.maxDelayMs() - config.minDelayMs() + 1);
                if (delay > 0) {
                    Thread.sleep(delay);
                }
            }
            
            // Maybe throw error
            if (random.nextDouble() < config.errorRate()) {
                String errorMessage = ERROR_MESSAGES[random.nextInt(ERROR_MESSAGES.length)];
                log.warn("Chaos Engineering: Injecting error - {}", errorMessage);
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                        "[Chaos Engineering] " + errorMessage);
            }
            
            return true;
        }
    }
}
