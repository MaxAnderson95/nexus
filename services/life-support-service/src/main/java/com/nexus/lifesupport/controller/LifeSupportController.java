package com.nexus.lifesupport.controller;

import com.nexus.lifesupport.dto.*;
import com.nexus.lifesupport.entity.Alert;
import com.nexus.lifesupport.service.LifeSupportService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/life-support")
public class LifeSupportController {
    
    private final LifeSupportService lifeSupportService;
    
    public LifeSupportController(LifeSupportService lifeSupportService) {
        this.lifeSupportService = lifeSupportService;
    }
    
    @GetMapping("/environment")
    public ResponseEntity<List<EnvironmentStatus>> getAllEnvironment() {
        return ResponseEntity.ok(lifeSupportService.getAllEnvironmentStatus());
    }
    
    @GetMapping("/environment/summary")
    public ResponseEntity<EnvironmentSummary> getEnvironmentSummary() {
        return ResponseEntity.ok(lifeSupportService.getEnvironmentSummary());
    }
    
    @GetMapping("/environment/section/{sectionId}")
    public ResponseEntity<EnvironmentStatus> getSectionEnvironment(@PathVariable Long sectionId) {
        return ResponseEntity.ok(lifeSupportService.getEnvironmentStatus(sectionId));
    }
    
    @PostMapping("/environment/section/{sectionId}/adjust")
    public ResponseEntity<EnvironmentStatus> adjustEnvironment(
            @PathVariable Long sectionId,
            @Valid @RequestBody AdjustEnvironmentRequest request) {
        return ResponseEntity.ok(lifeSupportService.adjustEnvironment(sectionId, request));
    }
    
    @PostMapping("/adjust-capacity")
    public ResponseEntity<Map<String, String>> adjustCapacity(@Valid @RequestBody AdjustCapacityRequest request) {
        lifeSupportService.adjustCapacity(request);
        return ResponseEntity.ok(Map.of("message", "Capacity adjusted successfully"));
    }
    
    @GetMapping("/alerts")
    public ResponseEntity<List<Alert>> getAlerts() {
        return ResponseEntity.ok(lifeSupportService.getAlerts());
    }
    
    @GetMapping("/alerts/all")
    public ResponseEntity<List<Alert>> getAllAlerts() {
        return ResponseEntity.ok(lifeSupportService.getAllAlerts());
    }
    
    @PostMapping("/alerts/{alertId}/acknowledge")
    public ResponseEntity<Alert> acknowledgeAlert(@PathVariable Long alertId) {
        return ResponseEntity.ok(lifeSupportService.acknowledgeAlert(alertId));
    }
    
    // Exception handlers
    @ExceptionHandler(LifeSupportService.SectionNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleSectionNotFound(LifeSupportService.SectionNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", ex.getMessage()));
    }
    
    @ExceptionHandler(LifeSupportService.AlertNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleAlertNotFound(LifeSupportService.AlertNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", ex.getMessage()));
    }
    
    @ExceptionHandler(LifeSupportService.CapacityExceededException.class)
    public ResponseEntity<Map<String, String>> handleCapacityExceeded(LifeSupportService.CapacityExceededException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", ex.getMessage()));
    }
}
