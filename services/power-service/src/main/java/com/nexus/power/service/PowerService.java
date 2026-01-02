package com.nexus.power.service;

import com.nexus.power.dto.*;
import com.nexus.power.entity.PowerAllocation;
import com.nexus.power.entity.PowerLog;
import com.nexus.power.entity.PowerSource;
import com.nexus.power.repository.PowerAllocationRepository;
import com.nexus.power.repository.PowerLogRepository;
import com.nexus.power.repository.PowerSourceRepository;
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
import java.util.List;
import java.util.Optional;

@Service
public class PowerService {
    
    private static final Logger log = LoggerFactory.getLogger(PowerService.class);
    
    private static final String REDIS_GRID_TOTAL = "power:grid:total";
    private static final String REDIS_GRID_ALLOCATED = "power:grid:allocated";
    
    private final PowerSourceRepository sourceRepository;
    private final PowerAllocationRepository allocationRepository;
    private final PowerLogRepository logRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final Tracer tracer;
    private final boolean customSpansEnabled;
    
    public PowerService(
            PowerSourceRepository sourceRepository,
            PowerAllocationRepository allocationRepository,
            PowerLogRepository logRepository,
            RedisTemplate<String, String> redisTemplate,
            Tracer tracer,
            @Value("${nexus.telemetry.custom-spans:false}") boolean customSpansEnabled) {
        this.sourceRepository = sourceRepository;
        this.allocationRepository = allocationRepository;
        this.logRepository = logRepository;
        this.redisTemplate = redisTemplate;
        this.tracer = tracer;
        this.customSpansEnabled = customSpansEnabled;
    }
    
    public PowerGridStatus getGridStatus() {
        if (customSpansEnabled) {
            Span span = tracer.spanBuilder("power.getGridStatus").startSpan();
            try (Scope scope = span.makeCurrent()) {
                PowerGridStatus status = buildGridStatus();
                span.setAttribute("power.total_capacity_kw", status.totalCapacityKw());
                span.setAttribute("power.utilization_percent", status.utilizationPercent());
                return status;
            } finally {
                span.end();
            }
        }
        return buildGridStatus();
    }
    
    private PowerGridStatus buildGridStatus() {
        List<PowerSource> sources = sourceRepository.findAll();
        
        double totalCapacity = sources.stream()
                .filter(s -> s.getStatus() == PowerSource.PowerSourceStatus.ONLINE)
                .mapToDouble(PowerSource::getMaxOutputKw)
                .sum();
        
        double totalOutput = sources.stream()
                .filter(s -> s.getStatus() == PowerSource.PowerSourceStatus.ONLINE)
                .mapToDouble(PowerSource::getCurrentOutputKw)
                .sum();
        
        Double totalAllocated = allocationRepository.getTotalAllocated();
        if (totalAllocated == null) totalAllocated = 0.0;
        
        double available = totalOutput - totalAllocated;
        double utilization = totalOutput > 0 ? (totalAllocated / totalOutput) * 100 : 0;
        
        long onlineSources = sources.stream()
                .filter(s -> s.getStatus() == PowerSource.PowerSourceStatus.ONLINE)
                .count();
        
        List<PowerGridStatus.PowerSourceSummary> sourceSummaries = sources.stream()
                .map(s -> new PowerGridStatus.PowerSourceSummary(
                        s.getId(),
                        s.getName(),
                        s.getType().name(),
                        s.getStatus().name(),
                        s.getMaxOutputKw(),
                        s.getCurrentOutputKw(),
                        s.getMaxOutputKw() > 0 
                                ? (s.getCurrentOutputKw() / s.getMaxOutputKw()) * 100 
                                : 0
                ))
                .toList();
        
        // Update Redis cache
        try {
            redisTemplate.opsForValue().set(REDIS_GRID_TOTAL, String.valueOf(totalOutput), Duration.ofMinutes(1));
            redisTemplate.opsForValue().set(REDIS_GRID_ALLOCATED, String.valueOf(totalAllocated), Duration.ofMinutes(1));
        } catch (Exception e) {
            log.warn("Failed to update Redis cache: {}", e.getMessage());
        }
        
        return new PowerGridStatus(
                totalCapacity,
                totalOutput,
                totalAllocated,
                available,
                utilization,
                (int) onlineSources,
                sources.size(),
                sourceSummaries
        );
    }
    
    public List<PowerSource> getAllSources() {
        return sourceRepository.findAll();
    }
    
    public Optional<PowerSource> getSourceById(Long id) {
        return sourceRepository.findById(id);
    }
    
    @Transactional
    public AllocationResponse allocatePower(AllocationRequest request) {
        if (customSpansEnabled) {
            Span span = tracer.spanBuilder("power.allocate")
                    .setAttribute("power.system", request.system())
                    .setAttribute("power.amount_kw", request.amountKw())
                    .startSpan();
            try (Scope scope = span.makeCurrent()) {
                span.addEvent("checking_available_power");
                AllocationResponse response = performAllocation(request);
                span.addEvent("power_allocated");
                span.setAttribute("power.allocation_id", response.id());
                return response;
            } finally {
                span.end();
            }
        }
        return performAllocation(request);
    }
    
    private AllocationResponse performAllocation(AllocationRequest request) {
        log.info("Allocating {} kW to system: {}", request.amountKw(), request.system());
        
        // Check available power
        PowerGridStatus grid = buildGridStatus();
        if (grid.availableKw() < request.amountKw()) {
            throw new InsufficientPowerException(
                    "Insufficient power available. Requested: " + request.amountKw() + 
                    " kW, Available: " + grid.availableKw() + " kW");
        }
        
        // Create or update allocation
        PowerAllocation allocation = allocationRepository.findBySystemName(request.system())
                .map(existing -> {
                    existing.setAllocatedKw(existing.getAllocatedKw() + request.amountKw());
                    if (request.priority() != null) {
                        existing.setPriority(request.priority());
                    }
                    return existing;
                })
                .orElseGet(() -> {
                    PowerAllocation newAllocation = new PowerAllocation();
                    newAllocation.setSystemName(request.system());
                    newAllocation.setAllocatedKw(request.amountKw());
                    newAllocation.setPriority(request.priority() != null ? request.priority() : 5);
                    newAllocation.setSectionId(request.sectionId());
                    return newAllocation;
                });
        
        allocation = allocationRepository.save(allocation);
        
        // Log the action
        PowerLog logEntry = new PowerLog();
        logEntry.setAction(PowerLog.PowerAction.ALLOCATE);
        logEntry.setAmountKw(request.amountKw());
        logEntry.setSystemName(request.system());
        logRepository.save(logEntry);
        
        log.info("Successfully allocated {} kW to system: {}, total allocation: {} kW",
                request.amountKw(), request.system(), allocation.getAllocatedKw());
        
        return new AllocationResponse(
                allocation.getId(),
                allocation.getSystemName(),
                allocation.getAllocatedKw(),
                allocation.getPriority(),
                allocation.getSectionId(),
                "Power allocated successfully"
        );
    }
    
    @Transactional
    public void deallocatePower(DeallocateRequest request) {
        if (customSpansEnabled) {
            Span span = tracer.spanBuilder("power.deallocate")
                    .setAttribute("power.system", request.system())
                    .startSpan();
            try (Scope scope = span.makeCurrent()) {
                performDeallocation(request);
                span.addEvent("power_deallocated");
            } finally {
                span.end();
            }
        } else {
            performDeallocation(request);
        }
    }
    
    private void performDeallocation(DeallocateRequest request) {
        log.info("Deallocating power from system: {}", request.system());

        Optional<PowerAllocation> allocationOpt = allocationRepository.findBySystemName(request.system());

        if (allocationOpt.isEmpty()) {
            log.info("No allocation found for system: {}, nothing to deallocate", request.system());
            return;
        }

        PowerAllocation allocation = allocationOpt.get();

        // Log the action
        PowerLog logEntry = new PowerLog();
        logEntry.setAction(PowerLog.PowerAction.DEALLOCATE);
        logEntry.setAmountKw(allocation.getAllocatedKw());
        logEntry.setSystemName(request.system());
        logRepository.save(logEntry);

        allocationRepository.delete(allocation);

        log.info("Successfully deallocated {} kW from system: {}",
                allocation.getAllocatedKw(), request.system());
    }
    
    public List<PowerAllocation> getAllAllocations() {
        return allocationRepository.findAllOrderByPriority();
    }
    
    public Optional<PowerAllocation> getAllocationBySystem(String systemName) {
        return allocationRepository.findBySystemName(systemName);
    }
    
    // Exception classes
    public static class InsufficientPowerException extends RuntimeException {
        public InsufficientPowerException(String message) {
            super(message);
        }
    }
    
    public static class AllocationNotFoundException extends RuntimeException {
        public AllocationNotFoundException(String message) {
            super(message);
        }
    }
}
