package com.nexus.cortex.controller;

import com.nexus.cortex.client.*;
import com.nexus.cortex.client.CrewClient.CrewSummary;
import com.nexus.cortex.client.DockingClient.DockingSummary;
import com.nexus.cortex.client.InventoryClient.InventorySummary;
import com.nexus.cortex.client.LifeSupportClient.LifeSupportSummary;
import com.nexus.cortex.client.PowerClient.PowerSummary;
import com.nexus.cortex.dto.DashboardStatus;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    
    private static final Logger log = LoggerFactory.getLogger(DashboardController.class);
    
    private final DockingClient dockingClient;
    private final CrewClient crewClient;
    private final LifeSupportClient lifeSupportClient;
    private final PowerClient powerClient;
    private final InventoryClient inventoryClient;
    private final Tracer tracer;
    private final ExecutorService executorService;
    
    public DashboardController(
            DockingClient dockingClient,
            CrewClient crewClient,
            LifeSupportClient lifeSupportClient,
            PowerClient powerClient,
            InventoryClient inventoryClient,
            Tracer tracer) {
        this.dockingClient = dockingClient;
        this.crewClient = crewClient;
        this.lifeSupportClient = lifeSupportClient;
        this.powerClient = powerClient;
        this.inventoryClient = inventoryClient;
        this.tracer = tracer;
        this.executorService = Executors.newFixedThreadPool(5);
    }
    
    @GetMapping("/status")
    public ResponseEntity<DashboardStatus> getDashboardStatus() {
        log.info("Fetching dashboard status - initiating parallel calls to all services");
        
        Span parentSpan = Span.current();
        Context parentContext = Context.current();
        
        // Fan-out: Make parallel calls to all 5 services
        CompletableFuture<DockingSummary> dockingFuture = CompletableFuture.supplyAsync(() -> {
            Span span = tracer.spanBuilder("fetch-docking-summary")
                    .setParent(parentContext)
                    .startSpan();
            try {
                return dockingClient.getSummary();
            } catch (Exception e) {
                log.error("Failed to fetch docking summary", e);
                span.recordException(e);
                return new DockingSummary(0, 0, 0, 0, 0, 0);
            } finally {
                span.end();
            }
        }, executorService);
        
        CompletableFuture<CrewSummary> crewFuture = CompletableFuture.supplyAsync(() -> {
            Span span = tracer.spanBuilder("fetch-crew-summary")
                    .setParent(parentContext)
                    .startSpan();
            try {
                return crewClient.getSummary();
            } catch (Exception e) {
                log.error("Failed to fetch crew summary", e);
                span.recordException(e);
                return new CrewSummary(0L, 0L, 0L, 0L, 0L);
            } finally {
                span.end();
            }
        }, executorService);
        
        CompletableFuture<LifeSupportSummary> lifeSupportFuture = CompletableFuture.supplyAsync(() -> {
            Span span = tracer.spanBuilder("fetch-life-support-summary")
                    .setParent(parentContext)
                    .startSpan();
            try {
                return lifeSupportClient.getSummary();
            } catch (Exception e) {
                log.error("Failed to fetch life support summary", e);
                span.recordException(e);
                return new LifeSupportSummary(0, 0, 0, 0, 0, 21.0, 22.0);
            } finally {
                span.end();
            }
        }, executorService);
        
        CompletableFuture<PowerSummary> powerFuture = CompletableFuture.supplyAsync(() -> {
            Span span = tracer.spanBuilder("fetch-power-summary")
                    .setParent(parentContext)
                    .startSpan();
            try {
                return powerClient.getSummary();
            } catch (Exception e) {
                log.error("Failed to fetch power summary", e);
                span.recordException(e);
                return new PowerSummary(0.0, 0.0, 0.0, 0.0, 0.0, 0, 0);
            } finally {
                span.end();
            }
        }, executorService);
        
        CompletableFuture<InventorySummary> inventoryFuture = CompletableFuture.supplyAsync(() -> {
            Span span = tracer.spanBuilder("fetch-inventory-summary")
                    .setParent(parentContext)
                    .startSpan();
            try {
                return inventoryClient.getSummary();
            } catch (Exception e) {
                log.error("Failed to fetch inventory summary", e);
                span.recordException(e);
                return new InventorySummary(0, 0, 0, 0);
            } finally {
                span.end();
            }
        }, executorService);
        
        // Fan-in: Wait for all futures to complete
        CompletableFuture.allOf(
                dockingFuture, crewFuture, lifeSupportFuture, powerFuture, inventoryFuture
        ).join();
        
        DockingSummary docking = dockingFuture.join();
        CrewSummary crew = crewFuture.join();
        LifeSupportSummary lifeSupport = lifeSupportFuture.join();
        PowerSummary power = powerFuture.join();
        InventorySummary inventory = inventoryFuture.join();
        
        DashboardStatus status = DashboardStatus.of(docking, crew, lifeSupport, power, inventory);
        
        log.info("Dashboard status assembled: overall={}", status.overallStatus());
        
        return ResponseEntity.ok(status);
    }
    
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getServiceHealth() {
        log.debug("Checking service health");
        
        Context parentContext = Context.current();
        
        CompletableFuture<Boolean> dockingHealth = CompletableFuture.supplyAsync(() -> {
            try {
                dockingClient.getAllBays();
                return true;
            } catch (Exception e) {
                return false;
            }
        }, executorService);
        
        CompletableFuture<Boolean> crewHealth = CompletableFuture.supplyAsync(() -> {
            try {
                crewClient.getCrewCount();
                return true;
            } catch (Exception e) {
                return false;
            }
        }, executorService);
        
        CompletableFuture<Boolean> lifeSupportHealth = CompletableFuture.supplyAsync(() -> {
            try {
                lifeSupportClient.getEnvironmentSummary();
                return true;
            } catch (Exception e) {
                return false;
            }
        }, executorService);
        
        CompletableFuture<Boolean> powerHealth = CompletableFuture.supplyAsync(() -> {
            try {
                powerClient.getGridStatus();
                return true;
            } catch (Exception e) {
                return false;
            }
        }, executorService);
        
        CompletableFuture<Boolean> inventoryHealth = CompletableFuture.supplyAsync(() -> {
            try {
                inventoryClient.getLowStockCount();
                return true;
            } catch (Exception e) {
                return false;
            }
        }, executorService);
        
        CompletableFuture.allOf(
                dockingHealth, crewHealth, lifeSupportHealth, powerHealth, inventoryHealth
        ).join();
        
        Map<String, Object> health = Map.of(
                "timestamp", Instant.now().toString(),
                "services", Map.of(
                        "docking", dockingHealth.join() ? "UP" : "DOWN",
                        "crew", crewHealth.join() ? "UP" : "DOWN",
                        "lifeSupport", lifeSupportHealth.join() ? "UP" : "DOWN",
                        "power", powerHealth.join() ? "UP" : "DOWN",
                        "inventory", inventoryHealth.join() ? "UP" : "DOWN"
                )
        );
        
        return ResponseEntity.ok(health);
    }
}
