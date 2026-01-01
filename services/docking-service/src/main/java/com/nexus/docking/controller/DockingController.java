package com.nexus.docking.controller;

import com.nexus.docking.dto.*;
import com.nexus.docking.service.DockingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/docking")
public class DockingController {
    
    private final DockingService dockingService;
    
    public DockingController(DockingService dockingService) {
        this.dockingService = dockingService;
    }
    
    @GetMapping("/bays")
    public ResponseEntity<List<DockingBayDto>> getAllBays() {
        return ResponseEntity.ok(dockingService.getAllBays());
    }
    
    @GetMapping("/bays/{id}")
    public ResponseEntity<DockingBayDto> getBayById(@PathVariable Long id) {
        return dockingService.getBayById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/ships")
    public ResponseEntity<List<ShipDto>> getAllShips() {
        return ResponseEntity.ok(dockingService.getAllShips());
    }
    
    @GetMapping("/ships/{id}")
    public ResponseEntity<ShipDto> getShipById(@PathVariable Long id) {
        return dockingService.getShipById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/ships/incoming")
    public ResponseEntity<List<ShipDto>> getIncomingShips() {
        return ResponseEntity.ok(dockingService.getIncomingShips());
    }
    
    @PostMapping("/dock/{shipId}")
    public ResponseEntity<DockResult> dockShip(@PathVariable Long shipId) {
        DockResult result = dockingService.dockShip(shipId);
        if (result.success()) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }
    
    @PostMapping("/undock/{shipId}")
    public ResponseEntity<DockResult> undockShip(@PathVariable Long shipId) {
        DockResult result = dockingService.undockShip(shipId);
        if (result.success()) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }
    
    @GetMapping("/logs")
    public ResponseEntity<List<DockingLogDto>> getDockingLogs() {
        return ResponseEntity.ok(dockingService.getDockingLogs());
    }
    
    @PostMapping("/schedule-delivery")
    public ResponseEntity<ShipDto> scheduleDelivery(@Valid @RequestBody ScheduleDeliveryRequest request) {
        ShipDto ship = dockingService.scheduleDelivery(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ship);
    }
    
    // Exception handlers
    @ExceptionHandler(DockingService.ShipNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleShipNotFound(DockingService.ShipNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", ex.getMessage()));
    }
    
    @ExceptionHandler(DockingService.BayNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleBayNotFound(DockingService.BayNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", ex.getMessage()));
    }
    
    @ExceptionHandler(DockingService.NoBayAvailableException.class)
    public ResponseEntity<Map<String, String>> handleNoBayAvailable(DockingService.NoBayAvailableException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", ex.getMessage()));
    }
}
