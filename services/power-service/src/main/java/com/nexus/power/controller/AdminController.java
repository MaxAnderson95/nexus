package com.nexus.power.controller;

import com.nexus.power.config.DataInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    
    private static final Logger log = LoggerFactory.getLogger(AdminController.class);
    
    private final DataInitializer dataInitializer;
    
    public AdminController(DataInitializer dataInitializer) {
        this.dataInitializer = dataInitializer;
    }
    
    @PostMapping("/resetTables")
    public ResponseEntity<Map<String, String>> resetTables() {
        log.info("Admin: Resetting tables for Power Service");
        dataInitializer.resetTables();
        return ResponseEntity.ok(Map.of(
            "service", "power",
            "status", "success",
            "message", "Power Service tables reset successfully"
        ));
    }
}
