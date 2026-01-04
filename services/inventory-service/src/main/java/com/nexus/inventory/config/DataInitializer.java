package com.nexus.inventory.config;

import com.nexus.inventory.entity.CargoItem;
import com.nexus.inventory.entity.CargoManifest;
import com.nexus.inventory.entity.ResupplyRequest;
import com.nexus.inventory.entity.Supply;
import com.nexus.inventory.repository.CargoItemRepository;
import com.nexus.inventory.repository.CargoManifestRepository;
import com.nexus.inventory.repository.ResupplyRequestRepository;
import com.nexus.inventory.repository.SupplyRepository;
import jakarta.persistence.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.UUID;

@Component
public class DataInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);
    private static final String INIT_LOCK_KEY = "init:lock:inventory";
    private static final Duration LOCK_TTL = Duration.ofMinutes(5);
    private static final Duration LOCK_WAIT_TIMEOUT = Duration.ofSeconds(30);

    private final SupplyRepository supplyRepository;
    private final CargoManifestRepository manifestRepository;
    private final CargoItemRepository cargoItemRepository;
    private final ResupplyRequestRepository resupplyRepository;
    private final EntityManager entityManager;
    private final RedisTemplate<String, String> redisTemplate;
    private final String instanceId;

    public DataInitializer(SupplyRepository supplyRepository,
                          CargoManifestRepository manifestRepository,
                          CargoItemRepository cargoItemRepository,
                          ResupplyRequestRepository resupplyRepository,
                          EntityManager entityManager,
                          RedisTemplate<String, String> redisTemplate) {
        this.supplyRepository = supplyRepository;
        this.manifestRepository = manifestRepository;
        this.cargoItemRepository = cargoItemRepository;
        this.resupplyRepository = resupplyRepository;
        this.entityManager = entityManager;
        this.redisTemplate = redisTemplate;
        this.instanceId = UUID.randomUUID().toString().substring(0, 8);
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        log.info("Checking demo data for Inventory Service...");

        if (!acquireInitLock()) {
            log.info("Another instance is initializing data, waiting for completion...");
            waitForInitCompletion();
            log.info("Inventory Service demo data check complete (initialized by another instance)");
            return;
        }

        try {
            if (supplyRepository.count() == 0) {
                log.info("Initializing supplies...");
                initializeSupplies();
            } else {
                log.info("Supplies already exist, skipping initialization");
            }

            if (manifestRepository.count() == 0) {
                log.info("Initializing cargo manifests...");
                initializeCargoManifests();
            } else {
                log.info("Cargo manifests already exist, skipping initialization");
            }

            if (resupplyRepository.count() == 0) {
                log.info("Initializing resupply requests...");
                initializeResupplyRequests();
            } else {
                log.info("Resupply requests already exist, skipping initialization");
            }
        } finally {
            releaseInitLock();
        }

        log.info("Inventory Service demo data check complete");
    }

    private boolean acquireInitLock() {
        try {
            Boolean acquired = redisTemplate.opsForValue()
                    .setIfAbsent(INIT_LOCK_KEY, instanceId, LOCK_TTL);
            return Boolean.TRUE.equals(acquired);
        } catch (Exception e) {
            log.warn("Failed to acquire Redis lock, proceeding without lock: {}", e.getMessage());
            return true;
        }
    }

    private void releaseInitLock() {
        try {
            redisTemplate.delete(INIT_LOCK_KEY);
        } catch (Exception e) {
            log.warn("Failed to release Redis lock: {}", e.getMessage());
        }
    }

    private void waitForInitCompletion() {
        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < LOCK_WAIT_TIMEOUT.toMillis()) {
            try {
                Thread.sleep(500);
                if (!Boolean.TRUE.equals(redisTemplate.hasKey(INIT_LOCK_KEY))) {
                    return;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
        log.warn("Timeout waiting for initialization lock to be released");
    }
    
    /**
     * Resets all tables and re-initializes demo data.
     */
    @Transactional
    public void resetTables() {
        log.info("Resetting Inventory Service tables...");
        
        // Delete in order respecting foreign key constraints (batch delete)
        resupplyRepository.deleteAllInBatch();
        cargoItemRepository.deleteAllInBatch();
        manifestRepository.deleteAllInBatch();
        supplyRepository.deleteAllInBatch();
        
        // Flush to ensure deletes are committed before inserts
        entityManager.flush();
        entityManager.clear();
        
        // Re-initialize demo data
        initializeSupplies();
        initializeCargoManifests();
        initializeResupplyRequests();
        
        log.info("Inventory Service tables reset complete");
    }
    
    private void initializeSupplies() {
        // FOOD supplies - All well-stocked
        createSupply("Emergency Rations", Supply.SupplyCategory.FOOD, 500, "units", 100, 1L);
        createSupply("Freeze-Dried Meals", Supply.SupplyCategory.FOOD, 1200, "packets", 200, 1L);
        createSupply("Protein Supplements", Supply.SupplyCategory.FOOD, 300, "containers", 50, 1L);
        createSupply("Fresh Produce", Supply.SupplyCategory.FOOD, 250, "kg", 100, 1L);
        
        // MEDICAL supplies - All well-stocked
        createSupply("First Aid Kits", Supply.SupplyCategory.MEDICAL, 50, "kits", 20, 2L);
        createSupply("Antibiotics", Supply.SupplyCategory.MEDICAL, 200, "doses", 100, 2L);
        createSupply("Pain Relievers", Supply.SupplyCategory.MEDICAL, 500, "doses", 150, 2L);
        createSupply("Surgical Supplies", Supply.SupplyCategory.MEDICAL, 30, "kits", 10, 2L);
        createSupply("Radiation Treatment Packs", Supply.SupplyCategory.MEDICAL, 50, "units", 20, 2L);
        
        // MECHANICAL supplies - All well-stocked
        createSupply("Replacement Seals", Supply.SupplyCategory.MECHANICAL, 150, "units", 50, 3L);
        createSupply("Lubricants", Supply.SupplyCategory.MECHANICAL, 80, "liters", 30, 3L);
        createSupply("Structural Bolts", Supply.SupplyCategory.MECHANICAL, 2000, "units", 500, 3L);
        createSupply("Pressure Valves", Supply.SupplyCategory.MECHANICAL, 75, "units", 30, 3L);
        
        // ELECTRONIC supplies - All well-stocked
        createSupply("Circuit Boards", Supply.SupplyCategory.ELECTRONIC, 100, "units", 40, 4L);
        createSupply("Power Regulators", Supply.SupplyCategory.ELECTRONIC, 45, "units", 20, 4L);
        createSupply("Sensor Arrays", Supply.SupplyCategory.ELECTRONIC, 30, "units", 15, 4L);
        createSupply("Data Cables", Supply.SupplyCategory.ELECTRONIC, 500, "meters", 100, 4L);
        createSupply("Backup Processors", Supply.SupplyCategory.ELECTRONIC, 25, "units", 10, 4L);
        
        // FUEL supplies - All well-stocked
        createSupply("Hydrogen Fuel Cells", Supply.SupplyCategory.FUEL, 200, "cells", 50, 5L);
        createSupply("Thruster Propellant", Supply.SupplyCategory.FUEL, 5000, "liters", 1000, 5L);
        createSupply("Fusion Reactor Fuel", Supply.SupplyCategory.FUEL, 150, "rods", 50, 5L);
        createSupply("Emergency Fuel Reserve", Supply.SupplyCategory.FUEL, 1500, "liters", 500, 5L);
        
        // WATER supplies - All well-stocked
        createSupply("Potable Water", Supply.SupplyCategory.WATER, 10000, "liters", 3000, 6L);
        createSupply("Water Purification Tablets", Supply.SupplyCategory.WATER, 5000, "tablets", 1000, 6L);
        createSupply("Water Recycler Filters", Supply.SupplyCategory.WATER, 50, "units", 20, 6L);
        
        // OXYGEN supplies - All well-stocked
        createSupply("Oxygen Canisters", Supply.SupplyCategory.OXYGEN, 300, "canisters", 100, 7L);
        createSupply("CO2 Scrubber Cartridges", Supply.SupplyCategory.OXYGEN, 120, "units", 50, 7L);
        createSupply("Emergency Oxygen Masks", Supply.SupplyCategory.OXYGEN, 200, "units", 80, 7L);
        
        // GENERAL supplies - All well-stocked
        createSupply("EVA Suit Components", Supply.SupplyCategory.GENERAL, 50, "sets", 20, 8L);
        createSupply("Tool Kits", Supply.SupplyCategory.GENERAL, 30, "kits", 15, 8L);
        
        log.info("Created 30 supplies");
    }
    
    private void createSupply(String name, Supply.SupplyCategory category, int quantity, 
                              String unit, int minThreshold, Long sectionId) {
        Supply supply = new Supply();
        supply.setName(name);
        supply.setCategory(category);
        supply.setQuantity(quantity);
        supply.setUnit(unit);
        supply.setMinThreshold(minThreshold);
        supply.setSectionId(sectionId);
        supplyRepository.save(supply);
    }
    
    private void initializeCargoManifests() {
        // Manifest 1: Pending food shipment
        CargoManifest manifest1 = new CargoManifest();
        manifest1.setShipId(1L);
        manifest1.setShipName("Supply Vessel Alpha");
        manifest1.setStatus(CargoManifest.ManifestStatus.PENDING);
        
        CargoItem item1a = new CargoItem();
        item1a.setSupplyName("Emergency Rations");
        item1a.setQuantity(200);
        manifest1.addItem(item1a);
        
        CargoItem item1b = new CargoItem();
        item1b.setSupplyName("Fresh Produce");
        item1b.setQuantity(150);
        manifest1.addItem(item1b);
        
        manifestRepository.save(manifest1);
        
        // Manifest 2: Pending medical shipment
        CargoManifest manifest2 = new CargoManifest();
        manifest2.setShipId(2L);
        manifest2.setShipName("Med Transport 7");
        manifest2.setStatus(CargoManifest.ManifestStatus.PENDING);
        
        CargoItem item2a = new CargoItem();
        item2a.setSupplyName("Radiation Treatment Packs");
        item2a.setQuantity(50);
        manifest2.addItem(item2a);
        
        CargoItem item2b = new CargoItem();
        item2b.setSupplyName("Antibiotics");
        item2b.setQuantity(300);
        manifest2.addItem(item2b);
        
        manifestRepository.save(manifest2);
        
        // Manifest 3: Pending equipment shipment
        CargoManifest manifest3 = new CargoManifest();
        manifest3.setShipId(3L);
        manifest3.setShipName("Cargo Hauler Delta");
        manifest3.setStatus(CargoManifest.ManifestStatus.PENDING);
        
        CargoItem item3a = new CargoItem();
        item3a.setSupplyName("Pressure Valves");
        item3a.setQuantity(40);
        manifest3.addItem(item3a);
        
        CargoItem item3b = new CargoItem();
        item3b.setSupplyName("Backup Processors");
        item3b.setQuantity(15);
        manifest3.addItem(item3b);
        
        CargoItem item3c = new CargoItem();
        item3c.setSupplyName("Circuit Boards");
        item3c.setQuantity(50);
        manifest3.addItem(item3c);
        
        manifestRepository.save(manifest3);
        
        log.info("Created 3 cargo manifests");
    }
    
    private void initializeResupplyRequests() {
        // Request 1: In transit
        ResupplyRequest request1 = new ResupplyRequest();
        request1.setSupplyId(4L); // Fresh Produce
        request1.setSupplyName("Fresh Produce");
        request1.setQuantity(100);
        request1.setStatus(ResupplyRequest.RequestStatus.IN_TRANSIT);
        resupplyRepository.save(request1);
        
        // Request 2: Approved
        ResupplyRequest request2 = new ResupplyRequest();
        request2.setSupplyId(9L); // Radiation Treatment Packs
        request2.setSupplyName("Radiation Treatment Packs");
        request2.setQuantity(30);
        request2.setStatus(ResupplyRequest.RequestStatus.APPROVED);
        resupplyRepository.save(request2);
        
        // Request 3: Pending
        ResupplyRequest request3 = new ResupplyRequest();
        request3.setSupplyId(13L); // Pressure Valves
        request3.setSupplyName("Pressure Valves");
        request3.setQuantity(25);
        request3.setStatus(ResupplyRequest.RequestStatus.PENDING);
        resupplyRepository.save(request3);
        
        log.info("Created 3 resupply requests");
    }
}
