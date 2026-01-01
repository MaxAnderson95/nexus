package com.nexus.power.config;

import com.nexus.power.entity.PowerAllocation;
import com.nexus.power.entity.PowerSource;
import com.nexus.power.repository.PowerAllocationRepository;
import com.nexus.power.repository.PowerSourceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DataInitializer implements ApplicationRunner {
    
    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);
    
    private final PowerSourceRepository sourceRepository;
    private final PowerAllocationRepository allocationRepository;
    
    public DataInitializer(PowerSourceRepository sourceRepository, 
                          PowerAllocationRepository allocationRepository) {
        this.sourceRepository = sourceRepository;
        this.allocationRepository = allocationRepository;
    }
    
    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        log.info("Checking demo data for Power Service...");
        
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
        
        log.info("Power Service demo data check complete");
    }
    
    private void initializePowerSources() {
        // Solar Array Alpha
        PowerSource solarAlpha = new PowerSource();
        solarAlpha.setName("Solar Array Alpha");
        solarAlpha.setType(PowerSource.PowerSourceType.SOLAR_ARRAY);
        solarAlpha.setMaxOutputKw(500.0);
        solarAlpha.setCurrentOutputKw(450.0);
        solarAlpha.setStatus(PowerSource.PowerSourceStatus.ONLINE);
        sourceRepository.save(solarAlpha);
        
        // Solar Array Beta
        PowerSource solarBeta = new PowerSource();
        solarBeta.setName("Solar Array Beta");
        solarBeta.setType(PowerSource.PowerSourceType.SOLAR_ARRAY);
        solarBeta.setMaxOutputKw(500.0);
        solarBeta.setCurrentOutputKw(480.0);
        solarBeta.setStatus(PowerSource.PowerSourceStatus.ONLINE);
        sourceRepository.save(solarBeta);
        
        // Fusion Reactor Core
        PowerSource fusionReactor = new PowerSource();
        fusionReactor.setName("Fusion Reactor Core");
        fusionReactor.setType(PowerSource.PowerSourceType.FUSION_REACTOR);
        fusionReactor.setMaxOutputKw(2000.0);
        fusionReactor.setCurrentOutputKw(1800.0);
        fusionReactor.setStatus(PowerSource.PowerSourceStatus.ONLINE);
        sourceRepository.save(fusionReactor);
        
        // Emergency Battery Bank
        PowerSource batteryBank = new PowerSource();
        batteryBank.setName("Emergency Battery Bank");
        batteryBank.setType(PowerSource.PowerSourceType.BATTERY_BANK);
        batteryBank.setMaxOutputKw(300.0);
        batteryBank.setCurrentOutputKw(0.0);
        batteryBank.setStatus(PowerSource.PowerSourceStatus.OFFLINE);
        sourceRepository.save(batteryBank);
        
        // Fuel Cell Array
        PowerSource fuelCell = new PowerSource();
        fuelCell.setName("Fuel Cell Array");
        fuelCell.setType(PowerSource.PowerSourceType.FUEL_CELL);
        fuelCell.setMaxOutputKw(200.0);
        fuelCell.setCurrentOutputKw(150.0);
        fuelCell.setStatus(PowerSource.PowerSourceStatus.DEGRADED);
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
