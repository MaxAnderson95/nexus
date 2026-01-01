package com.nexus.inventory.service;

import com.nexus.inventory.client.CrewClient;
import com.nexus.inventory.client.DockingClient;
import com.nexus.inventory.dto.*;
import com.nexus.inventory.entity.CargoManifest;
import com.nexus.inventory.entity.ResupplyRequest;
import com.nexus.inventory.entity.Supply;
import com.nexus.inventory.repository.CargoManifestRepository;
import com.nexus.inventory.repository.ResupplyRequestRepository;
import com.nexus.inventory.repository.SupplyRepository;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
public class InventoryService {
    
    private static final Logger log = LoggerFactory.getLogger(InventoryService.class);
    
    private static final String REDIS_LOW_STOCK_COUNT = "inventory:low_stock:count";
    private static final String REDIS_TOTAL_SUPPLIES = "inventory:total_supplies";
    
    private final SupplyRepository supplyRepository;
    private final CargoManifestRepository manifestRepository;
    private final ResupplyRequestRepository resupplyRepository;
    private final DockingClient dockingClient;
    private final CrewClient crewClient;
    private final RedisTemplate<String, String> redisTemplate;
    private final Tracer tracer;
    private final boolean customSpansEnabled;
    
    public InventoryService(
            SupplyRepository supplyRepository,
            CargoManifestRepository manifestRepository,
            ResupplyRequestRepository resupplyRepository,
            DockingClient dockingClient,
            CrewClient crewClient,
            RedisTemplate<String, String> redisTemplate,
            Tracer tracer,
            @Value("${nexus.telemetry.custom-spans:false}") boolean customSpansEnabled) {
        this.supplyRepository = supplyRepository;
        this.manifestRepository = manifestRepository;
        this.resupplyRepository = resupplyRepository;
        this.dockingClient = dockingClient;
        this.crewClient = crewClient;
        this.redisTemplate = redisTemplate;
        this.tracer = tracer;
        this.customSpansEnabled = customSpansEnabled;
    }
    
    public List<SupplyDto> getAllSupplies() {
        if (customSpansEnabled) {
            Span span = tracer.spanBuilder("inventory.getAllSupplies").startSpan();
            try (Scope scope = span.makeCurrent()) {
                List<SupplyDto> supplies = fetchAllSupplies();
                span.setAttribute("inventory.supply_count", supplies.size());
                return supplies;
            } finally {
                span.end();
            }
        }
        return fetchAllSupplies();
    }
    
    private List<SupplyDto> fetchAllSupplies() {
        List<Supply> supplies = supplyRepository.findAll();
        
        // Update Redis cache
        try {
            redisTemplate.opsForValue().set(REDIS_TOTAL_SUPPLIES, String.valueOf(supplies.size()), Duration.ofMinutes(5));
        } catch (Exception e) {
            log.warn("Failed to update Redis cache: {}", e.getMessage());
        }
        
        return supplies.stream()
                .map(SupplyDto::fromEntity)
                .toList();
    }
    
    public SupplyDto getSupplyById(Long id) {
        return supplyRepository.findById(id)
                .map(SupplyDto::fromEntity)
                .orElse(null);
    }
    
    public List<SupplyDto> getLowStockSupplies() {
        if (customSpansEnabled) {
            Span span = tracer.spanBuilder("inventory.getLowStockSupplies").startSpan();
            try (Scope scope = span.makeCurrent()) {
                List<SupplyDto> lowStock = fetchLowStockSupplies();
                span.setAttribute("inventory.low_stock_count", lowStock.size());
                return lowStock;
            } finally {
                span.end();
            }
        }
        return fetchLowStockSupplies();
    }
    
    private List<SupplyDto> fetchLowStockSupplies() {
        List<Supply> lowStockSupplies = supplyRepository.findLowStockSupplies();
        
        // Update Redis cache
        try {
            redisTemplate.opsForValue().set(REDIS_LOW_STOCK_COUNT, String.valueOf(lowStockSupplies.size()), Duration.ofMinutes(1));
        } catch (Exception e) {
            log.warn("Failed to update Redis cache: {}", e.getMessage());
        }
        
        return lowStockSupplies.stream()
                .map(SupplyDto::fromEntity)
                .toList();
    }
    
    public long getLowStockCount() {
        // Try Redis cache first
        try {
            String cached = redisTemplate.opsForValue().get(REDIS_LOW_STOCK_COUNT);
            if (cached != null) {
                return Long.parseLong(cached);
            }
        } catch (Exception e) {
            log.warn("Failed to read from Redis cache: {}", e.getMessage());
        }
        
        return supplyRepository.countLowStockSupplies();
    }
    
    @Transactional
    public SupplyDto consumeSupply(ConsumeRequest request) {
        if (customSpansEnabled) {
            Span span = tracer.spanBuilder("inventory.consumeSupply")
                    .setAttribute("inventory.supply_id", request.supplyId())
                    .setAttribute("inventory.quantity", request.quantity())
                    .startSpan();
            try (Scope scope = span.makeCurrent()) {
                span.addEvent("checking_supply_availability");
                SupplyDto result = performConsumeSupply(request);
                span.addEvent("supply_consumed");
                return result;
            } finally {
                span.end();
            }
        }
        return performConsumeSupply(request);
    }
    
    private SupplyDto performConsumeSupply(ConsumeRequest request) {
        log.info("Consuming {} units of supply ID: {}", request.quantity(), request.supplyId());
        
        Supply supply = supplyRepository.findById(request.supplyId())
                .orElseThrow(() -> new SupplyNotFoundException("Supply not found: " + request.supplyId()));
        
        if (supply.getQuantity() < request.quantity()) {
            throw new InsufficientSupplyException(
                    "Insufficient quantity. Available: " + supply.getQuantity() + 
                    ", Requested: " + request.quantity());
        }
        
        supply.setQuantity(supply.getQuantity() - request.quantity());
        supply = supplyRepository.save(supply);
        
        log.info("Consumed {} units of '{}', remaining: {}", 
                request.quantity(), supply.getName(), supply.getQuantity());
        
        // Invalidate cache
        try {
            redisTemplate.delete(REDIS_LOW_STOCK_COUNT);
        } catch (Exception e) {
            log.warn("Failed to invalidate Redis cache: {}", e.getMessage());
        }
        
        return SupplyDto.fromEntity(supply);
    }
    
    @Transactional
    public ResupplyRequestDto requestResupply(CreateResupplyRequest request) {
        if (customSpansEnabled) {
            Span span = tracer.spanBuilder("inventory.requestResupply")
                    .setAttribute("inventory.supply_id", request.supplyId())
                    .setAttribute("inventory.quantity", request.quantity())
                    .startSpan();
            try (Scope scope = span.makeCurrent()) {
                span.addEvent("creating_resupply_request");
                ResupplyRequestDto result = performRequestResupply(request);
                span.addEvent("resupply_request_created");
                span.setAttribute("inventory.resupply_id", result.id());
                return result;
            } finally {
                span.end();
            }
        }
        return performRequestResupply(request);
    }
    
    private ResupplyRequestDto performRequestResupply(CreateResupplyRequest request) {
        log.info("Creating resupply request for supply ID: {}, quantity: {}", 
                request.supplyId(), request.quantity());
        
        Supply supply = supplyRepository.findById(request.supplyId())
                .orElseThrow(() -> new SupplyNotFoundException("Supply not found: " + request.supplyId()));
        
        // Create resupply request
        ResupplyRequest resupplyRequest = new ResupplyRequest();
        resupplyRequest.setSupplyId(supply.getId());
        resupplyRequest.setSupplyName(supply.getName());
        resupplyRequest.setQuantity(request.quantity());
        resupplyRequest.setStatus(ResupplyRequest.RequestStatus.PENDING);
        
        resupplyRequest = resupplyRepository.save(resupplyRequest);
        
        // Call docking service to schedule delivery
        try {
            DockingClient.ScheduleDeliveryResponse deliveryResponse = 
                    dockingClient.scheduleDelivery(supply.getName(), request.quantity());
            
            log.info("Delivery scheduled: {}", deliveryResponse);
            
            // Update status to approved after successful scheduling
            resupplyRequest.setStatus(ResupplyRequest.RequestStatus.APPROVED);
            resupplyRequest = resupplyRepository.save(resupplyRequest);
        } catch (DockingClient.DockingServiceException e) {
            log.warn("Failed to schedule delivery, resupply request remains pending: {}", e.getMessage());
            // Request stays in PENDING status
        }
        
        log.info("Created resupply request ID: {} for '{}'", 
                resupplyRequest.getId(), supply.getName());
        
        return ResupplyRequestDto.fromEntity(resupplyRequest);
    }
    
    public List<ResupplyRequestDto> getResupplyRequests() {
        return resupplyRepository.findAllOrderByRequestedAtDesc().stream()
                .map(ResupplyRequestDto::fromEntity)
                .toList();
    }
    
    public List<CargoManifestDto> getCargoManifests() {
        return manifestRepository.findAllOrderByCreatedAtDesc().stream()
                .map(CargoManifestDto::fromEntity)
                .toList();
    }
    
    public CargoManifestDto getCargoManifestById(Long id) {
        return manifestRepository.findById(id)
                .map(CargoManifestDto::fromEntity)
                .orElse(null);
    }
    
    @Transactional
    public CargoManifestDto unloadManifest(Long manifestId) {
        if (customSpansEnabled) {
            Span span = tracer.spanBuilder("inventory.unloadManifest")
                    .setAttribute("inventory.manifest_id", manifestId)
                    .startSpan();
            try (Scope scope = span.makeCurrent()) {
                span.addEvent("starting_manifest_unload");
                CargoManifestDto result = performUnloadManifest(manifestId);
                span.addEvent("manifest_unloaded");
                return result;
            } finally {
                span.end();
            }
        }
        return performUnloadManifest(manifestId);
    }
    
    private CargoManifestDto performUnloadManifest(Long manifestId) {
        log.info("Starting unload of cargo manifest ID: {}", manifestId);
        
        CargoManifest manifest = manifestRepository.findById(manifestId)
                .orElseThrow(() -> new ManifestNotFoundException("Manifest not found: " + manifestId));
        
        if (manifest.getStatus() != CargoManifest.ManifestStatus.PENDING) {
            throw new InvalidManifestStateException(
                    "Cannot unload manifest in status: " + manifest.getStatus());
        }
        
        // Get available crew for unloading
        try {
            var availableCrew = crewClient.getAvailableCrew();
            log.info("Assigned {} crew members for unloading manifest {}", 
                    availableCrew.size(), manifestId);
        } catch (CrewClient.CrewServiceException e) {
            log.warn("Could not fetch crew assignment, proceeding with unload: {}", e.getMessage());
        }
        
        // Update manifest status to UNLOADING
        manifest.setStatus(CargoManifest.ManifestStatus.UNLOADING);
        manifest = manifestRepository.save(manifest);
        
        // Process each cargo item
        for (var item : manifest.getItems()) {
            if (item.getSupplyId() != null) {
                supplyRepository.findById(item.getSupplyId()).ifPresent(supply -> {
                    supply.setQuantity(supply.getQuantity() + item.getQuantity());
                    supplyRepository.save(supply);
                    log.info("Added {} units of '{}' to inventory", 
                            item.getQuantity(), supply.getName());
                });
            }
        }
        
        // Mark manifest as completed
        manifest.setStatus(CargoManifest.ManifestStatus.COMPLETED);
        manifest.setCompletedAt(Instant.now());
        manifest = manifestRepository.save(manifest);
        
        // Invalidate cache
        try {
            redisTemplate.delete(REDIS_LOW_STOCK_COUNT);
            redisTemplate.delete(REDIS_TOTAL_SUPPLIES);
        } catch (Exception e) {
            log.warn("Failed to invalidate Redis cache: {}", e.getMessage());
        }
        
        log.info("Completed unload of cargo manifest ID: {}", manifestId);
        
        return CargoManifestDto.fromEntity(manifest);
    }
    
    // Exception classes
    public static class SupplyNotFoundException extends RuntimeException {
        public SupplyNotFoundException(String message) {
            super(message);
        }
    }
    
    public static class InsufficientSupplyException extends RuntimeException {
        public InsufficientSupplyException(String message) {
            super(message);
        }
    }
    
    public static class ManifestNotFoundException extends RuntimeException {
        public ManifestNotFoundException(String message) {
            super(message);
        }
    }
    
    public static class InvalidManifestStateException extends RuntimeException {
        public InvalidManifestStateException(String message) {
            super(message);
        }
    }
}
