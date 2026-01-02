package com.nexus.docking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DockingServiceApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(DockingServiceApplication.class, args);
    }
}
