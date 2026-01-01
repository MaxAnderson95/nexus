package com.nexus.cortex.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

import java.io.IOException;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve static files and handle SPA routing
        registry.addResourceHandler("/**")
                .addResourceLocations("file:/app/static/", "classpath:/static/")
                .resourceChain(true)
                .addResolver(new PathResourceResolver() {
                    @Override
                    protected Resource getResource(String resourcePath, Resource location) throws IOException {
                        Resource resource = location.createRelative(resourcePath);
                        
                        // If the resource exists and is readable, return it
                        if (resource.exists() && resource.isReadable()) {
                            return resource;
                        }
                        
                        // For SPA routing: return index.html for non-API, non-file routes
                        if (!resourcePath.startsWith("api/") && 
                            !resourcePath.startsWith("actuator/") &&
                            !resourcePath.contains(".")) {
                            // Try file location first, then classpath
                            Resource indexResource = new FileSystemResource("/app/static/index.html");
                            if (indexResource.exists()) {
                                return indexResource;
                            }
                            return new ClassPathResource("/static/index.html");
                        }
                        
                        return null;
                    }
                });
    }
}
