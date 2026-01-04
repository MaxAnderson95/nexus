package com.nexus.docking.config;

import com.nexus.docking.entity.DockingBay;
import com.nexus.docking.entity.DockingLog;
import com.nexus.docking.entity.Ship;
import com.nexus.docking.repository.DockingBayRepository;
import com.nexus.docking.repository.DockingLogRepository;
import com.nexus.docking.repository.ShipRepository;
import jakarta.persistence.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Component
public class DataInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);
    private static final String INIT_LOCK_KEY = "init:lock:docking";
    private static final Duration LOCK_TTL = Duration.ofMinutes(5);
    private static final Duration LOCK_WAIT_TIMEOUT = Duration.ofSeconds(30);

    private final DockingBayRepository bayRepository;
    private final ShipRepository shipRepository;
    private final DockingLogRepository logRepository;
    private final EntityManager entityManager;
    private final RedisTemplate<String, String> redisTemplate;
    private final String instanceId;

    public DataInitializer(DockingBayRepository bayRepository,
                          ShipRepository shipRepository,
                          DockingLogRepository logRepository,
                          EntityManager entityManager,
                          RedisTemplate<String, String> redisTemplate) {
        this.bayRepository = bayRepository;
        this.shipRepository = shipRepository;
        this.logRepository = logRepository;
        this.entityManager = entityManager;
        this.redisTemplate = redisTemplate;
        this.instanceId = UUID.randomUUID().toString().substring(0, 8);
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        log.info("Checking demo data for Docking Service...");

        if (!acquireInitLock()) {
            log.info("Another instance is initializing data, waiting for completion...");
            waitForInitCompletion();
            log.info("Docking Service demo data check complete (initialized by another instance)");
            return;
        }

        try {
            if (shipRepository.count() == 0) {
                log.info("Initializing ships...");
                initializeShips();
            } else {
                log.info("Ships already exist, skipping initialization");
            }

            if (bayRepository.count() == 0) {
                log.info("Initializing docking bays...");
                initializeDockingBays();
            } else {
                log.info("Docking bays already exist, skipping initialization");
            }
        } finally {
            releaseInitLock();
        }

        log.info("Docking Service demo data check complete");
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
        log.info("Resetting Docking Service tables...");
        
        // Delete in order respecting foreign key constraints (batch delete)
        logRepository.deleteAllInBatch();
        bayRepository.deleteAllInBatch();
        shipRepository.deleteAllInBatch();
        
        // Flush to ensure deletes are committed before inserts
        entityManager.flush();
        entityManager.clear();
        
        // Re-initialize demo data
        initializeShips();
        initializeDockingBays();
        
        log.info("Docking Service tables reset complete");
    }
    
    private void initializeShips() {
        // Docked ships (4)
        createShip("MSV Stellar Hauler", Ship.ShipType.CARGO, 12, 5000, Ship.ShipStatus.DOCKED, -2);
        createShip("CSV Pioneer", Ship.ShipType.PASSENGER, 45, 500, Ship.ShipStatus.DOCKED, -5);
        createShip("RSS Quantum", Ship.ShipType.SUPPLY, 8, 3500, Ship.ShipStatus.DOCKED, -1);
        createShip("SRV Horizon", Ship.ShipType.RESEARCH, 18, 1200, Ship.ShipStatus.DOCKED, -3);

        // Departing ships (2)
        createShip("MSV Trade Wind", Ship.ShipType.CARGO, 10, 4500, Ship.ShipStatus.DEPARTING, -6);
        createShip("CSV Wanderer", Ship.ShipType.PASSENGER, 38, 400, Ship.ShipStatus.DEPARTING, -8);

        // Incoming ships (19)
        createShip("RSS Provision", Ship.ShipType.SUPPLY, 8, 3000, Ship.ShipStatus.INCOMING, 30);
        createShip("SRV Discovery", Ship.ShipType.RESEARCH, 25, 1000, Ship.ShipStatus.INCOMING, 45);
        createShip("UNS Defender", Ship.ShipType.MILITARY, 150, 800, Ship.ShipStatus.INCOMING, 60);
        createShip("MSV Iron Clad", Ship.ShipType.CARGO, 14, 6000, Ship.ShipStatus.INCOMING, 75);
        createShip("CSV Starliner", Ship.ShipType.PASSENGER, 120, 600, Ship.ShipStatus.INCOMING, 90);
        createShip("RSS Bounty", Ship.ShipType.SUPPLY, 6, 2500, Ship.ShipStatus.INCOMING, 105);
        createShip("SRV Pathfinder", Ship.ShipType.RESEARCH, 22, 900, Ship.ShipStatus.INCOMING, 120);
        createShip("MSV Nebula Runner", Ship.ShipType.CARGO, 11, 4800, Ship.ShipStatus.INCOMING, 135);
        createShip("UNS Vigilant", Ship.ShipType.MILITARY, 85, 700, Ship.ShipStatus.INCOMING, 150);
        createShip("CSV Aurora", Ship.ShipType.PASSENGER, 95, 550, Ship.ShipStatus.INCOMING, 165);
        createShip("RSS Harvest", Ship.ShipType.SUPPLY, 9, 3200, Ship.ShipStatus.INCOMING, 180);
        createShip("SRV Endeavor", Ship.ShipType.RESEARCH, 30, 1100, Ship.ShipStatus.INCOMING, 200);
        createShip("MSV Titan", Ship.ShipType.CARGO, 16, 7000, Ship.ShipStatus.INCOMING, 220);
        createShip("CSV Meridian", Ship.ShipType.PASSENGER, 75, 480, Ship.ShipStatus.INCOMING, 240);
        createShip("UNS Sentinel", Ship.ShipType.MILITARY, 110, 850, Ship.ShipStatus.INCOMING, 260);
        createShip("RSS Stockpile", Ship.ShipType.SUPPLY, 7, 2800, Ship.ShipStatus.INCOMING, 280);
        createShip("SRV Curiosity", Ship.ShipType.RESEARCH, 28, 950, Ship.ShipStatus.INCOMING, 300);
        createShip("MSV Freighter Nine", Ship.ShipType.CARGO, 13, 5500, Ship.ShipStatus.INCOMING, 320);
        createShip("CSV Nomad", Ship.ShipType.PASSENGER, 55, 420, Ship.ShipStatus.INCOMING, 340);

        log.info("Created 25 ships (4 docked, 2 departing, 19 incoming)");
    }

    private Ship createShip(String name, Ship.ShipType type, int crewCount, int cargoCapacity,
                            Ship.ShipStatus status, int minutesOffset) {
        Ship ship = new Ship();
        ship.setName(name);
        ship.setType(type);
        ship.setCrewCount(crewCount);
        ship.setCargoCapacity(cargoCapacity);
        ship.setStatus(status);
        if (minutesOffset < 0) {
            ship.setArrivalTime(Instant.now().minus(-minutesOffset, ChronoUnit.MINUTES));
        } else {
            ship.setArrivalTime(Instant.now().plus(minutesOffset, ChronoUnit.MINUTES));
        }
        return shipRepository.save(ship);
    }
    
    private void initializeDockingBays() {
        // Get the docked ships for assignment
        var dockedShips = shipRepository.findByStatus(Ship.ShipStatus.DOCKED);

        // Bay capacities for variety
        int[] capacities = {6000, 4000, 5000, 3500, 5000, 8000, 4500, 6000, 3000, 5500, 7000, 4000};

        // Create 12 bays - first 4 occupied by docked ships, rest available
        for (int i = 0; i < 12; i++) {
            DockingBay bay = new DockingBay();
            bay.setBayNumber(i + 1);
            bay.setCapacity(capacities[i]);

            if (i < dockedShips.size()) {
                // Occupied by docked ship
                bay.setStatus(DockingBay.BayStatus.OCCUPIED);
                bay.setCurrentShipId(dockedShips.get(i).getId());
            } else {
                // Available
                bay.setStatus(DockingBay.BayStatus.AVAILABLE);
            }

            DockingBay savedBay = bayRepository.save(bay);

            // Create docking log for occupied bays
            if (i < dockedShips.size()) {
                DockingLog dockLog = new DockingLog();
                dockLog.setShipId(dockedShips.get(i).getId());
                dockLog.setBayId(savedBay.getId());
                dockLog.setAction(DockingLog.DockingAction.DOCK);
                logRepository.save(dockLog);
            }
        }

        log.info("Created 12 docking bays (4 occupied, 8 available)");
    }
}
