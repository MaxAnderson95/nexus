package com.nexus.docking.service;

import com.nexus.docking.client.CrewClient;
import com.nexus.docking.client.PowerClient;
import com.nexus.docking.dto.*;
import com.nexus.docking.entity.DockingBay;
import com.nexus.docking.entity.DockingLog;
import com.nexus.docking.entity.Ship;
import com.nexus.docking.repository.DockingBayRepository;
import com.nexus.docking.repository.DockingLogRepository;
import com.nexus.docking.repository.ShipRepository;
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
import java.util.Optional;

@Service
public class DockingService {
    
    private static final Logger log = LoggerFactory.getLogger(DockingService.class);
    
    private static final String REDIS_BAYS_AVAILABLE = "docking:bays:available";
    private static final String REDIS_SHIPS_INCOMING = "docking:ships:incoming";
    private static final double POWER_PER_BAY_KW = 50.0;
    
    private final DockingBayRepository bayRepository;
    private final ShipRepository shipRepository;
    private final DockingLogRepository logRepository;
    private final PowerClient powerClient;
    private final CrewClient crewClient;
    private final RedisTemplate<String, String> redisTemplate;
    private final Tracer tracer;
    private final boolean customSpansEnabled;
    
    public DockingService(
            DockingBayRepository bayRepository,
            ShipRepository shipRepository,
            DockingLogRepository logRepository,
            PowerClient powerClient,
            CrewClient crewClient,
            RedisTemplate<String, String> redisTemplate,
            Tracer tracer,
            @Value("${nexus.telemetry.custom-spans:false}") boolean customSpansEnabled) {
        this.bayRepository = bayRepository;
        this.shipRepository = shipRepository;
        this.logRepository = logRepository;
        this.powerClient = powerClient;
        this.crewClient = crewClient;
        this.redisTemplate = redisTemplate;
        this.tracer = tracer;
        this.customSpansEnabled = customSpansEnabled;
    }
    
    public List<DockingBayDto> getAllBays() {
        if (customSpansEnabled) {
            Span span = tracer.spanBuilder("docking.getAllBays").startSpan();
            try (Scope scope = span.makeCurrent()) {
                List<DockingBayDto> bays = bayRepository.findAll().stream()
                        .map(DockingBayDto::fromEntity)
                        .toList();
                span.setAttribute("docking.bay_count", bays.size());
                return bays;
            } finally {
                span.end();
            }
        }
        return bayRepository.findAll().stream()
                .map(DockingBayDto::fromEntity)
                .toList();
    }
    
    public Optional<DockingBayDto> getBayById(Long id) {
        return bayRepository.findById(id).map(DockingBayDto::fromEntity);
    }
    
    public List<ShipDto> getAllShips() {
        return shipRepository.findAll().stream()
                .map(ShipDto::fromEntity)
                .toList();
    }
    
    public List<ShipDto> getIncomingShips() {
        if (customSpansEnabled) {
            Span span = tracer.spanBuilder("docking.getIncomingShips").startSpan();
            try (Scope scope = span.makeCurrent()) {
                List<ShipDto> ships = shipRepository.findByStatusOrderByArrivalTimeAsc(Ship.ShipStatus.INCOMING)
                        .stream()
                        .map(ShipDto::fromEntity)
                        .toList();
                span.setAttribute("docking.incoming_count", ships.size());
                
                // Update Redis cache
                try {
                    redisTemplate.opsForValue().set(REDIS_SHIPS_INCOMING, 
                            String.valueOf(ships.size()), Duration.ofMinutes(1));
                } catch (Exception e) {
                    log.warn("Failed to update Redis cache: {}", e.getMessage());
                }
                
                return ships;
            } finally {
                span.end();
            }
        }
        return shipRepository.findByStatusOrderByArrivalTimeAsc(Ship.ShipStatus.INCOMING)
                .stream()
                .map(ShipDto::fromEntity)
                .toList();
    }
    
    @Transactional
    public DockResult dockShip(Long shipId) {
        if (customSpansEnabled) {
            Span span = tracer.spanBuilder("docking.dockShip")
                    .setAttribute("docking.ship_id", shipId)
                    .startSpan();
            try (Scope scope = span.makeCurrent()) {
                span.addEvent("finding_ship");
                DockResult result = performDocking(shipId);
                if (result.success()) {
                    span.setAttribute("docking.bay_id", result.bayId());
                    span.addEvent("docking_complete");
                } else {
                    span.addEvent("docking_failed");
                }
                return result;
            } finally {
                span.end();
            }
        }
        return performDocking(shipId);
    }
    
    private DockResult performDocking(Long shipId) {
        log.info("Attempting to dock ship with ID: {}", shipId);
        
        // Find the ship
        Ship ship = shipRepository.findById(shipId)
                .orElseThrow(() -> new ShipNotFoundException("Ship not found with ID: " + shipId));
        
        if (ship.getStatus() == Ship.ShipStatus.DOCKED) {
            return DockResult.failure(shipId, "Ship is already docked");
        }
        
        // Find an available bay
        DockingBay bay = bayRepository.findFirstByStatusOrderByBayNumberAsc(DockingBay.BayStatus.AVAILABLE)
                .orElseThrow(() -> new NoBayAvailableException("No docking bay available"));
        
        log.info("Allocating bay {} for ship '{}'", bay.getBayNumber(), ship.getName());
        
        // Allocate power for the bay (distributed call)
        try {
            powerClient.allocatePowerForBay(bay.getId(), POWER_PER_BAY_KW);
        } catch (PowerClient.PowerAllocationException e) {
            log.error("Failed to allocate power for docking bay: {}", e.getMessage());
            return DockResult.failure(shipId, "Failed to allocate power for docking bay: " + e.getMessage());
        }
        
        // Update bay status
        bay.setStatus(DockingBay.BayStatus.OCCUPIED);
        bay.setCurrentShipId(shipId);
        bayRepository.save(bay);
        
        // Update ship status
        ship.setStatus(Ship.ShipStatus.DOCKED);
        ship.setArrivalTime(Instant.now());
        shipRepository.save(ship);
        
        // Log the docking action
        DockingLog dockLog = new DockingLog();
        dockLog.setShipId(shipId);
        dockLog.setBayId(bay.getId());
        dockLog.setAction(DockingLog.DockingAction.DOCK);
        logRepository.save(dockLog);
        
        // Register crew arrival if ship has crew (distributed call)
        if (ship.getCrewCount() > 0) {
            crewClient.registerArrival(ship.getName(), ship.getCrewCount());
        }
        
        // Update Redis cache
        updateBayCache();
        
        log.info("Ship '{}' successfully docked at bay {}", ship.getName(), bay.getBayNumber());
        return DockResult.success(bay.getId(), shipId, 
                String.format("Ship '%s' docked successfully at bay %d", ship.getName(), bay.getBayNumber()));
    }
    
    @Transactional
    public DockResult undockShip(Long shipId) {
        if (customSpansEnabled) {
            Span span = tracer.spanBuilder("docking.undockShip")
                    .setAttribute("docking.ship_id", shipId)
                    .startSpan();
            try (Scope scope = span.makeCurrent()) {
                span.addEvent("finding_ship");
                DockResult result = performUndocking(shipId);
                if (result.success()) {
                    span.addEvent("undocking_complete");
                } else {
                    span.addEvent("undocking_failed");
                }
                return result;
            } finally {
                span.end();
            }
        }
        return performUndocking(shipId);
    }
    
    private DockResult performUndocking(Long shipId) {
        log.info("Attempting to undock ship with ID: {}", shipId);
        
        // Find the ship
        Ship ship = shipRepository.findById(shipId)
                .orElseThrow(() -> new ShipNotFoundException("Ship not found with ID: " + shipId));
        
        if (ship.getStatus() != Ship.ShipStatus.DOCKED) {
            return DockResult.failure(shipId, "Ship is not currently docked");
        }
        
        // Find the bay the ship is in
        DockingBay bay = bayRepository.findByCurrentShipId(shipId)
                .orElseThrow(() -> new BayNotFoundException("Cannot find bay for docked ship"));
        
        log.info("Undocking ship '{}' from bay {}", ship.getName(), bay.getBayNumber());
        
        // Register crew departure if ship has crew (distributed call)
        if (ship.getCrewCount() > 0) {
            crewClient.registerDeparture(ship.getName());
        }
        
        // Deallocate power for the bay (distributed call)
        powerClient.deallocatePowerForBay(bay.getId());
        
        // Update bay status
        bay.setStatus(DockingBay.BayStatus.AVAILABLE);
        bay.setCurrentShipId(null);
        bayRepository.save(bay);
        
        // Update ship status
        ship.setStatus(Ship.ShipStatus.DEPARTING);
        shipRepository.save(ship);
        
        // Log the undocking action
        DockingLog undockLog = new DockingLog();
        undockLog.setShipId(shipId);
        undockLog.setBayId(bay.getId());
        undockLog.setAction(DockingLog.DockingAction.UNDOCK);
        logRepository.save(undockLog);
        
        // Update Redis cache
        updateBayCache();
        
        log.info("Ship '{}' successfully undocked from bay {}", ship.getName(), bay.getBayNumber());
        return DockResult.success(bay.getId(), shipId, 
                String.format("Ship '%s' undocked successfully from bay %d", ship.getName(), bay.getBayNumber()));
    }
    
    public List<DockingLogDto> getDockingLogs() {
        return logRepository.findAllByOrderByTimestampDesc().stream()
                .map(DockingLogDto::fromEntity)
                .toList();
    }
    
    @Transactional
    public ShipDto scheduleDelivery(ScheduleDeliveryRequest request) {
        if (customSpansEnabled) {
            Span span = tracer.spanBuilder("docking.scheduleDelivery")
                    .setAttribute("docking.ship_name", request.shipName())
                    .setAttribute("docking.cargo_type", request.cargoType())
                    .startSpan();
            try (Scope scope = span.makeCurrent()) {
                ShipDto result = performScheduleDelivery(request);
                span.setAttribute("docking.ship_id", result.id());
                span.addEvent("delivery_scheduled");
                return result;
            } finally {
                span.end();
            }
        }
        return performScheduleDelivery(request);
    }
    
    private ShipDto performScheduleDelivery(ScheduleDeliveryRequest request) {
        log.info("Scheduling delivery from ship '{}' with cargo type '{}'", 
                request.shipName(), request.cargoType());
        
        // Create new ship entry
        Ship ship = new Ship();
        ship.setName(request.shipName());
        ship.setType(Ship.ShipType.CARGO);  // Deliveries are always cargo ships
        ship.setCrewCount(5);  // Default crew count for delivery ships
        ship.setCargoCapacity(2000);  // Default cargo capacity
        ship.setStatus(Ship.ShipStatus.INCOMING);
        ship.setArrivalTime(request.estimatedArrival());
        ship = shipRepository.save(ship);
        
        // Log the scheduled arrival
        DockingLog arrivalLog = new DockingLog();
        arrivalLog.setShipId(ship.getId());
        arrivalLog.setAction(DockingLog.DockingAction.ARRIVAL_SCHEDULED);
        logRepository.save(arrivalLog);
        
        log.info("Delivery scheduled: ship '{}' expected at {}", ship.getName(), ship.getArrivalTime());
        return ShipDto.fromEntity(ship);
    }
    
    public Optional<ShipDto> getShipById(Long id) {
        return shipRepository.findById(id).map(ShipDto::fromEntity);
    }
    
    private void updateBayCache() {
        try {
            long availableBays = bayRepository.countByStatus(DockingBay.BayStatus.AVAILABLE);
            redisTemplate.opsForValue().set(REDIS_BAYS_AVAILABLE, 
                    String.valueOf(availableBays), Duration.ofMinutes(1));
        } catch (Exception e) {
            log.warn("Failed to update Redis cache: {}", e.getMessage());
        }
    }
    
    // Exception classes
    public static class ShipNotFoundException extends RuntimeException {
        public ShipNotFoundException(String message) {
            super(message);
        }
    }
    
    public static class BayNotFoundException extends RuntimeException {
        public BayNotFoundException(String message) {
            super(message);
        }
    }
    
    public static class NoBayAvailableException extends RuntimeException {
        public NoBayAvailableException(String message) {
            super(message);
        }
    }
}
