package com.nexus.cortex.controller;

import com.nexus.cortex.client.LifeSupportClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static com.nexus.cortex.validation.RequestValidator.*;

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
        validatePositiveId(sectionId, "Section ID");
        return ResponseEntity.ok(lifeSupportClient.getSectionEnvironment(sectionId));
    }

    @PostMapping("/environment/section/{sectionId}/adjust")
    public ResponseEntity<Map<String, Object>> adjustEnvironment(
            @PathVariable Long sectionId,
            @RequestBody Map<String, Object> request) {
        validatePositiveId(sectionId, "Section ID");
        if (request.containsKey("o2Level")) {
            validateRange(request.get("o2Level"), "o2Level", 0, 100);
        }
        if (request.containsKey("temperature")) {
            validateRange(request.get("temperature"), "temperature", -50, 100);
        }
        if (request.containsKey("pressure")) {
            validateRange(request.get("pressure"), "pressure", 0, 200);
        }
        if (request.containsKey("humidity")) {
            validateRange(request.get("humidity"), "humidity", 0, 100);
        }
        return ResponseEntity.ok(lifeSupportClient.adjustEnvironment(sectionId, request));
    }

    @PostMapping("/adjust-capacity")
    public ResponseEntity<Map<String, Object>> adjustCapacity(@RequestBody Map<String, Object> request) {
        validateRequired(request, "sectionId", "newCapacity");
        validatePositiveNumber(request.get("sectionId"), "sectionId");
        validatePositiveNumber(request.get("newCapacity"), "newCapacity");
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
        validatePositiveId(alertId, "Alert ID");
        return ResponseEntity.ok(lifeSupportClient.acknowledgeAlert(alertId));
    }

    @PostMapping("/environment/section/{sectionId}/self-test")
    public ResponseEntity<Map<String, Object>> runSelfTest(@PathVariable Long sectionId) {
        validatePositiveId(sectionId, "Section ID");
        return ResponseEntity.ok(lifeSupportClient.runSelfTest(sectionId));
    }
}
