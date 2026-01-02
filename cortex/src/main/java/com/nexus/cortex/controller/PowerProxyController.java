package com.nexus.cortex.controller;

import com.nexus.cortex.client.PowerClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static com.nexus.cortex.validation.RequestValidator.*;

@RestController
@RequestMapping("/api/power")
public class PowerProxyController {

    private final PowerClient powerClient;

    public PowerProxyController(PowerClient powerClient) {
        this.powerClient = powerClient;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getPowerStatus() {
        return ResponseEntity.ok(powerClient.getPowerStatus());
    }

    @GetMapping("/grid")
    public ResponseEntity<Map<String, Object>> getGridStatus() {
        return ResponseEntity.ok(powerClient.getGridStatus());
    }

    @GetMapping("/sources")
    public ResponseEntity<List<Map<String, Object>>> getAllSources() {
        return ResponseEntity.ok(powerClient.getAllSources());
    }

    @GetMapping("/sources/{id}")
    public ResponseEntity<Map<String, Object>> getSourceById(@PathVariable Long id) {
        validatePositiveId(id, "Source ID");
        return ResponseEntity.ok(powerClient.getSourceById(id));
    }

    @PostMapping("/allocate")
    public ResponseEntity<Map<String, Object>> allocatePower(@RequestBody Map<String, Object> request) {
        validateRequired(request, "system", "amountKw");
        validateNotBlank((String) request.get("system"), "system");
        validatePositiveNumber(request.get("amountKw"), "amountKw");
        return ResponseEntity.ok(powerClient.allocatePower(request));
    }

    @PostMapping("/deallocate")
    public ResponseEntity<Map<String, Object>> deallocatePower(@RequestBody Map<String, Object> request) {
        validateRequired(request, "system");
        validateNotBlank((String) request.get("system"), "system");
        return ResponseEntity.ok(powerClient.deallocatePower(request));
    }

    @GetMapping("/allocations")
    public ResponseEntity<List<Map<String, Object>>> getAllAllocations() {
        return ResponseEntity.ok(powerClient.getAllAllocations());
    }

    @GetMapping("/allocation/{system}")
    public ResponseEntity<Map<String, Object>> getAllocationBySystem(@PathVariable String system) {
        validateNotBlank(system, "System");
        return ResponseEntity.ok(powerClient.getAllocationBySystem(system));
    }
}
