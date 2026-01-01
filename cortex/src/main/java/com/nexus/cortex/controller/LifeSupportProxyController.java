package com.nexus.cortex.controller;

import com.nexus.cortex.client.LifeSupportClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/life-support")
public class LifeSupportProxyController {
    
    private final LifeSupportClient lifeSupportClient;
    
    public LifeSupportProxyController(LifeSupportClient lifeSupportClient) {
        this.lifeSupportClient = lifeSupportClient;
    }
    
    @GetMapping("/environment")
    public ResponseEntity<List<Map<String, Object>>> getAllEnvironment() {
        return ResponseEntity.ok(lifeSupportClient.getAllEnvironment());
    }
    
    @GetMapping("/environment/summary")
    public ResponseEntity<Map<String, Object>> getEnvironmentSummary() {
        return ResponseEntity.ok(lifeSupportClient.getEnvironmentSummary());
    }
    
    @GetMapping("/environment/section/{sectionId}")
    public ResponseEntity<Map<String, Object>> getSectionEnvironment(@PathVariable Long sectionId) {
        return ResponseEntity.ok(lifeSupportClient.getSectionEnvironment(sectionId));
    }
    
    @PostMapping("/environment/section/{sectionId}/adjust")
    public ResponseEntity<Map<String, Object>> adjustEnvironment(
            @PathVariable Long sectionId,
            @RequestBody Map<String, Object> request) {
        return ResponseEntity.ok(lifeSupportClient.adjustEnvironment(sectionId, request));
    }
    
    @PostMapping("/adjust-capacity")
    public ResponseEntity<Map<String, Object>> adjustCapacity(@RequestBody Map<String, Object> request) {
        return ResponseEntity.ok(lifeSupportClient.adjustCapacity(request));
    }
    
    @GetMapping("/alerts")
    public ResponseEntity<List<Map<String, Object>>> getAlerts() {
        return ResponseEntity.ok(lifeSupportClient.getAlerts());
    }
    
    @GetMapping("/alerts/all")
    public ResponseEntity<List<Map<String, Object>>> getAllAlerts() {
        return ResponseEntity.ok(lifeSupportClient.getAllAlerts());
    }
    
    @PostMapping("/alerts/{alertId}/acknowledge")
    public ResponseEntity<Map<String, Object>> acknowledgeAlert(@PathVariable Long alertId) {
        return ResponseEntity.ok(lifeSupportClient.acknowledgeAlert(alertId));
    }
}
