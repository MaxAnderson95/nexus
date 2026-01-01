package com.nexus.power.controller;

import com.nexus.power.dto.*;
import com.nexus.power.entity.PowerAllocation;
import com.nexus.power.entity.PowerSource;
import com.nexus.power.service.PowerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/power")
public class PowerController {
    
    private final PowerService powerService;
    
    public PowerController(PowerService powerService) {
        this.powerService = powerService;
    }
    
    @GetMapping
    public ResponseEntity<PowerGridStatus> getPowerStatus() {
        return ResponseEntity.ok(powerService.getGridStatus());
    }
    
    @GetMapping("/grid")
    public ResponseEntity<PowerGridStatus> getGridStatus() {
        return ResponseEntity.ok(powerService.getGridStatus());
    }
    
    @GetMapping("/sources")
    public ResponseEntity<List<PowerSource>> getAllSources() {
        return ResponseEntity.ok(powerService.getAllSources());
    }
    
    @GetMapping("/sources/{id}")
    public ResponseEntity<PowerSource> getSourceById(@PathVariable Long id) {
        return powerService.getSourceById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping("/allocate")
    public ResponseEntity<AllocationResponse> allocatePower(@Valid @RequestBody AllocationRequest request) {
        AllocationResponse response = powerService.allocatePower(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @PostMapping("/deallocate")
    public ResponseEntity<Map<String, String>> deallocatePower(@Valid @RequestBody DeallocateRequest request) {
        powerService.deallocatePower(request);
        return ResponseEntity.ok(Map.of("message", "Power deallocated successfully"));
    }
    
    @GetMapping("/allocations")
    public ResponseEntity<List<PowerAllocation>> getAllAllocations() {
        return ResponseEntity.ok(powerService.getAllAllocations());
    }
    
    @GetMapping("/allocation/{system}")
    public ResponseEntity<PowerAllocation> getAllocationBySystem(@PathVariable String system) {
        return powerService.getAllocationBySystem(system)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    // Exception handlers
    @ExceptionHandler(PowerService.InsufficientPowerException.class)
    public ResponseEntity<Map<String, String>> handleInsufficientPower(PowerService.InsufficientPowerException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", ex.getMessage()));
    }
    
    @ExceptionHandler(PowerService.AllocationNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleAllocationNotFound(PowerService.AllocationNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", ex.getMessage()));
    }
}
