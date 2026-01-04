package com.nexus.power.config;

import com.nexus.power.entity.PowerAllocation;
import com.nexus.power.entity.PowerSource;
import com.nexus.power.repository.PowerAllocationRepository;
import com.nexus.power.repository.PowerLogRepository;
import com.nexus.power.repository.PowerSourceRepository;
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
    private static final String INIT_LOCK_KEY = "init:lock:power";
    private static final Duration LOCK_TTL = Duration.ofMinutes(5);
    private static final Duration LOCK_WAIT_TIMEOUT = Duration.ofSeconds(30);

    private final PowerSourceRepository sourceRepository;
    private final PowerAllocationRepository allocationRepository;
    private final PowerLogRepository logRepository;
    private final EntityManager entityManager;
    private final RedisTemplate<String, String> redisTemplate;
    private final String instanceId;

    public DataInitializer(PowerSourceRepository sourceRepository,
                          PowerAllocationRepository allocationRepository,
                          PowerLogRepository logRepository,
                          EntityManager entityManager,
                          RedisTemplate<String, String> redisTemplate) {
        this.sourceRepository = sourceRepository;
        this.allocationRepository = allocationRepository;
        this.logRepository = logRepository;
        this.entityManager = entityManager;
        this.redisTemplate = redisTemplate;
        this.instanceId = UUID.randomUUID().toString().substring(0, 8);
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        log.info("Checking demo data for Power Service...");

        if (!acquireInitLock()) {
            log.info("Another instance is initializing data, waiting for completion...");
            waitForInitCompletion();
            log.info("Power Service demo data check complete (initialized by another instance)");
            return;
        }

        try {
            if (sourceRepository.count() == 0) {
                log.info("Initializing power sources...");
                initializePowerSources();
            } else {
                log.info("Power sources already exist, skipping initialization");
            }

            if (allocationRepository.count() == 0) {
                log.info("Initializing power allocations...");
                initializePowerAllocations();
            } else {
                log.info("Power allocations already exist, skipping initialization");
            }
        } finally {
            releaseInitLock();
        }

        log.info("Power Service demo data check complete");
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
        log.info("Resetting Power Service tables...");
        
        // Delete in order respecting foreign key constraints (batch delete)
        logRepository.deleteAllInBatch();
        allocationRepository.deleteAllInBatch();
        sourceRepository.deleteAllInBatch();
        
        // Flush to ensure deletes are committed before inserts
        entityManager.flush();
        entityManager.clear();
        
        // Re-initialize demo data
        initializePowerSources();
        initializePowerAllocations();
        
        log.info("Power Service tables reset complete");
    }
    
    private void initializePowerSources() {
        // Solar Array Alpha - High capacity for demo stability
        PowerSource solarAlpha = new PowerSource();
        solarAlpha.setName("Solar Array Alpha");
        solarAlpha.setType(PowerSource.PowerSourceType.SOLAR_ARRAY);
        solarAlpha.setMaxOutputKw(5000.0);
        solarAlpha.setCurrentOutputKw(4500.0);
        solarAlpha.setStatus(PowerSource.PowerSourceStatus.ONLINE);
        sourceRepository.save(solarAlpha);
        
        // Solar Array Beta - High capacity for demo stability
        PowerSource solarBeta = new PowerSource();
        solarBeta.setName("Solar Array Beta");
        solarBeta.setType(PowerSource.PowerSourceType.SOLAR_ARRAY);
        solarBeta.setMaxOutputKw(5000.0);
        solarBeta.setCurrentOutputKw(4800.0);
        solarBeta.setStatus(PowerSource.PowerSourceStatus.ONLINE);
        sourceRepository.save(solarBeta);
        
        // Fusion Reactor Core - Primary power source with massive capacity
        PowerSource fusionReactor = new PowerSource();
        fusionReactor.setName("Fusion Reactor Core");
        fusionReactor.setType(PowerSource.PowerSourceType.FUSION_REACTOR);
        fusionReactor.setMaxOutputKw(50000.0);
        fusionReactor.setCurrentOutputKw(45000.0);
        fusionReactor.setStatus(PowerSource.PowerSourceStatus.ONLINE);
        sourceRepository.save(fusionReactor);
        
        // Emergency Battery Bank - Standby reserve
        PowerSource batteryBank = new PowerSource();
        batteryBank.setName("Emergency Battery Bank");
        batteryBank.setType(PowerSource.PowerSourceType.BATTERY_BANK);
        batteryBank.setMaxOutputKw(10000.0);
        batteryBank.setCurrentOutputKw(0.0);
        batteryBank.setStatus(PowerSource.PowerSourceStatus.STANDBY);
        sourceRepository.save(batteryBank);
        
        // Fuel Cell Array - Online and contributing
        PowerSource fuelCell = new PowerSource();
        fuelCell.setName("Fuel Cell Array");
        fuelCell.setType(PowerSource.PowerSourceType.FUEL_CELL);
        fuelCell.setMaxOutputKw(8000.0);
        fuelCell.setCurrentOutputKw(7000.0);
        fuelCell.setStatus(PowerSource.PowerSourceStatus.ONLINE);
        sourceRepository.save(fuelCell);
        
        log.info("Created 5 power sources");
    }
    
    private void initializePowerAllocations() {
        // Life Support - highest priority
        PowerAllocation lifeSupport = new PowerAllocation();
        lifeSupport.setSystemName("life_support");
        lifeSupport.setAllocatedKw(800.0);
        lifeSupport.setPriority(1);
        allocationRepository.save(lifeSupport);
        
        // Station Core Systems
        PowerAllocation coreSystems = new PowerAllocation();
        coreSystems.setSystemName("core_systems");
        coreSystems.setAllocatedKw(400.0);
        coreSystems.setPriority(2);
        allocationRepository.save(coreSystems);
        
        // Communications
        PowerAllocation communications = new PowerAllocation();
        communications.setSystemName("communications");
        communications.setAllocatedKw(150.0);
        communications.setPriority(3);
        allocationRepository.save(communications);
        
        // Docking Systems
        PowerAllocation docking = new PowerAllocation();
        docking.setSystemName("docking");
        docking.setAllocatedKw(200.0);
        docking.setPriority(4);
        allocationRepository.save(docking);
        
        // Research Labs
        PowerAllocation research = new PowerAllocation();
        research.setSystemName("research");
        research.setAllocatedKw(300.0);
        research.setPriority(5);
        allocationRepository.save(research);
        
        // Sensors
        PowerAllocation sensors = new PowerAllocation();
        sensors.setSystemName("sensors");
        sensors.setAllocatedKw(100.0);
        sensors.setPriority(4);
        allocationRepository.save(sensors);
        
        // Defense Systems
        PowerAllocation defense = new PowerAllocation();
        defense.setSystemName("defense");
        defense.setAllocatedKw(250.0);
        defense.setPriority(2);
        allocationRepository.save(defense);
        
        log.info("Created 7 power allocations");
    }
}
