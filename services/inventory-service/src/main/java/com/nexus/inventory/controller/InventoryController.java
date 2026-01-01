package com.nexus.inventory.controller;

import com.nexus.inventory.dto.*;
import com.nexus.inventory.service.InventoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {
    
    private final InventoryService inventoryService;
    
    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }
    
    @GetMapping
    public ResponseEntity<Map<String, Object>> getInventoryStatus() {
        List<SupplyDto> supplies = inventoryService.getAllSupplies();
        long lowStockCount = inventoryService.getLowStockCount();
        
        return ResponseEntity.ok(Map.of(
                "totalSupplies", supplies.size(),
                "lowStockCount", lowStockCount,
                "supplies", supplies
        ));
    }
    
    @GetMapping("/supplies")
    public ResponseEntity<List<SupplyDto>> getAllSupplies() {
        return ResponseEntity.ok(inventoryService.getAllSupplies());
    }
    
    @GetMapping("/supplies/{id}")
    public ResponseEntity<SupplyDto> getSupplyById(@PathVariable Long id) {
        SupplyDto supply = inventoryService.getSupplyById(id);
        if (supply == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(supply);
    }
    
    @GetMapping("/supplies/low-stock")
    public ResponseEntity<List<SupplyDto>> getLowStockSupplies() {
        return ResponseEntity.ok(inventoryService.getLowStockSupplies());
    }
    
    @GetMapping("/supplies/low-stock/count")
    public ResponseEntity<Map<String, Long>> getLowStockCount() {
        return ResponseEntity.ok(Map.of("count", inventoryService.getLowStockCount()));
    }
    
    @PostMapping("/consume")
    public ResponseEntity<SupplyDto> consumeSupply(@Valid @RequestBody ConsumeRequest request) {
        SupplyDto result = inventoryService.consumeSupply(request);
        return ResponseEntity.ok(result);
    }
    
    @PostMapping("/resupply")
    public ResponseEntity<ResupplyRequestDto> requestResupply(@Valid @RequestBody CreateResupplyRequest request) {
        ResupplyRequestDto result = inventoryService.requestResupply(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }
    
    @GetMapping("/resupply-requests")
    public ResponseEntity<List<ResupplyRequestDto>> getResupplyRequests() {
        return ResponseEntity.ok(inventoryService.getResupplyRequests());
    }
    
    @GetMapping("/cargo-manifests")
    public ResponseEntity<List<CargoManifestDto>> getCargoManifests() {
        return ResponseEntity.ok(inventoryService.getCargoManifests());
    }
    
    @GetMapping("/cargo-manifests/{id}")
    public ResponseEntity<CargoManifestDto> getCargoManifestById(@PathVariable Long id) {
        CargoManifestDto manifest = inventoryService.getCargoManifestById(id);
        if (manifest == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(manifest);
    }
    
    @PostMapping("/cargo-manifests/{id}/unload")
    public ResponseEntity<CargoManifestDto> unloadManifest(@PathVariable Long id) {
        CargoManifestDto result = inventoryService.unloadManifest(id);
        return ResponseEntity.ok(result);
    }
    
    // Exception handlers
    @ExceptionHandler(InventoryService.SupplyNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleSupplyNotFound(InventoryService.SupplyNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", ex.getMessage()));
    }
    
    @ExceptionHandler(InventoryService.InsufficientSupplyException.class)
    public ResponseEntity<Map<String, String>> handleInsufficientSupply(InventoryService.InsufficientSupplyException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", ex.getMessage()));
    }
    
    @ExceptionHandler(InventoryService.ManifestNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleManifestNotFound(InventoryService.ManifestNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", ex.getMessage()));
    }
    
    @ExceptionHandler(InventoryService.InvalidManifestStateException.class)
    public ResponseEntity<Map<String, String>> handleInvalidManifestState(InventoryService.InvalidManifestStateException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", ex.getMessage()));
    }
}
