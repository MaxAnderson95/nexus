package com.nexus.cortex.controller;

import com.nexus.cortex.client.DockingClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static com.nexus.cortex.validation.RequestValidator.*;

@RestController
@RequestMapping("/api/docking")
public class DockingProxyController {

    private final DockingClient dockingClient;

    public DockingProxyController(DockingClient dockingClient) {
        this.dockingClient = dockingClient;
    }

    @GetMapping("/bays")
    public ResponseEntity<List<Map<String, Object>>> getAllBays() {
        return ResponseEntity.ok(dockingClient.getAllBays());
    }

    @GetMapping("/bays/{id}")
    public ResponseEntity<Map<String, Object>> getBayById(@PathVariable Long id) {
        validatePositiveId(id, "Bay ID");
        return ResponseEntity.ok(dockingClient.getBayById(id));
    }

    @GetMapping("/ships")
    public ResponseEntity<List<Map<String, Object>>> getAllShips() {
        return ResponseEntity.ok(dockingClient.getAllShips());
    }

    @GetMapping("/ships/{id}")
    public ResponseEntity<Map<String, Object>> getShipById(@PathVariable Long id) {
        validatePositiveId(id, "Ship ID");
        return ResponseEntity.ok(dockingClient.getShipById(id));
    }

    @GetMapping("/ships/incoming")
    public ResponseEntity<List<Map<String, Object>>> getIncomingShips() {
        return ResponseEntity.ok(dockingClient.getIncomingShips());
    }

    @PostMapping("/dock/{shipId}")
    public ResponseEntity<Map<String, Object>> dockShip(@PathVariable Long shipId) {
        validatePositiveId(shipId, "Ship ID");
        return ResponseEntity.ok(dockingClient.dockShip(shipId));
    }

    @PostMapping("/undock/{shipId}")
    public ResponseEntity<Map<String, Object>> undockShip(@PathVariable Long shipId) {
        validatePositiveId(shipId, "Ship ID");
        return ResponseEntity.ok(dockingClient.undockShip(shipId));
    }

    @GetMapping("/logs")
    public ResponseEntity<List<Map<String, Object>>> getDockingLogs() {
        return ResponseEntity.ok(dockingClient.getDockingLogs());
    }

    @PostMapping("/schedule-delivery")
    public ResponseEntity<Map<String, Object>> scheduleDelivery(@RequestBody Map<String, Object> request) {
        validateRequired(request, "shipId", "deliveryTime");
        validatePositiveNumber(request.get("shipId"), "shipId");
        return ResponseEntity.ok(dockingClient.scheduleDelivery(request));
    }
}
