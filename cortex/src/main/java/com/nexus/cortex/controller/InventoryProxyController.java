package com.nexus.cortex.controller;

import com.nexus.cortex.client.InventoryClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inventory")
public class InventoryProxyController {
    
    private final InventoryClient inventoryClient;
    
    public InventoryProxyController(InventoryClient inventoryClient) {
        this.inventoryClient = inventoryClient;
    }
    
    @GetMapping
    public ResponseEntity<Map<String, Object>> getInventoryStatus() {
        return ResponseEntity.ok(inventoryClient.getInventoryStatus());
    }
    
    @GetMapping("/supplies")
    public ResponseEntity<List<Map<String, Object>>> getAllSupplies() {
        return ResponseEntity.ok(inventoryClient.getAllSupplies());
    }
    
    @GetMapping("/supplies/{id}")
    public ResponseEntity<Map<String, Object>> getSupplyById(@PathVariable Long id) {
        return ResponseEntity.ok(inventoryClient.getSupplyById(id));
    }
    
    @GetMapping("/supplies/low-stock")
    public ResponseEntity<List<Map<String, Object>>> getLowStockSupplies() {
        return ResponseEntity.ok(inventoryClient.getLowStockSupplies());
    }
    
    @GetMapping("/supplies/low-stock/count")
    public ResponseEntity<Map<String, Object>> getLowStockCount() {
        return ResponseEntity.ok(inventoryClient.getLowStockCount());
    }
    
    @PostMapping("/consume")
    public ResponseEntity<Map<String, Object>> consumeSupply(@RequestBody Map<String, Object> request) {
        return ResponseEntity.ok(inventoryClient.consumeSupply(request));
    }
    
    @PostMapping("/resupply")
    public ResponseEntity<Map<String, Object>> requestResupply(@RequestBody Map<String, Object> request) {
        return ResponseEntity.ok(inventoryClient.requestResupply(request));
    }
    
    @GetMapping("/resupply-requests")
    public ResponseEntity<List<Map<String, Object>>> getResupplyRequests() {
        return ResponseEntity.ok(inventoryClient.getResupplyRequests());
    }
    
    @GetMapping("/cargo-manifests")
    public ResponseEntity<List<Map<String, Object>>> getCargoManifests() {
        return ResponseEntity.ok(inventoryClient.getCargoManifests());
    }
    
    @GetMapping("/cargo-manifests/{id}")
    public ResponseEntity<Map<String, Object>> getCargoManifestById(@PathVariable Long id) {
        return ResponseEntity.ok(inventoryClient.getCargoManifestById(id));
    }
    
    @PostMapping("/cargo-manifests/{id}/unload")
    public ResponseEntity<Map<String, Object>> unloadManifest(@PathVariable Long id) {
        return ResponseEntity.ok(inventoryClient.unloadManifest(id));
    }
}
