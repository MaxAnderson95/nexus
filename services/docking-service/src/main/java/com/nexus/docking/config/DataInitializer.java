package com.nexus.docking.config;

import com.nexus.docking.entity.DockingBay;
import com.nexus.docking.entity.DockingLog;
import com.nexus.docking.entity.Ship;
import com.nexus.docking.repository.DockingBayRepository;
import com.nexus.docking.repository.DockingLogRepository;
import com.nexus.docking.repository.ShipRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Component
public class DataInitializer implements ApplicationRunner {
    
    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);
    
    private final DockingBayRepository bayRepository;
    private final ShipRepository shipRepository;
    private final DockingLogRepository logRepository;
    
    public DataInitializer(DockingBayRepository bayRepository, 
                          ShipRepository shipRepository,
                          DockingLogRepository logRepository) {
        this.bayRepository = bayRepository;
        this.shipRepository = shipRepository;
        this.logRepository = logRepository;
    }
    
    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        log.info("Checking demo data for Docking Service...");
        
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
        
        log.info("Docking Service demo data check complete");
    }
    
    private void initializeShips() {
        // Docked ships (2)
        Ship cargoShip1 = new Ship();
        cargoShip1.setName("MSV Stellar Hauler");
        cargoShip1.setType(Ship.ShipType.CARGO);
        cargoShip1.setCrewCount(12);
        cargoShip1.setCargoCapacity(5000);
        cargoShip1.setStatus(Ship.ShipStatus.DOCKED);
        cargoShip1.setArrivalTime(Instant.now().minus(2, ChronoUnit.HOURS));
        shipRepository.save(cargoShip1);
        
        Ship passengerShip = new Ship();
        passengerShip.setName("CSV Pioneer");
        passengerShip.setType(Ship.ShipType.PASSENGER);
        passengerShip.setCrewCount(45);
        passengerShip.setCargoCapacity(500);
        passengerShip.setStatus(Ship.ShipStatus.DOCKED);
        passengerShip.setArrivalTime(Instant.now().minus(5, ChronoUnit.HOURS));
        shipRepository.save(passengerShip);
        
        // Incoming ships (3)
        Ship supplyShip = new Ship();
        supplyShip.setName("RSS Provision");
        supplyShip.setType(Ship.ShipType.SUPPLY);
        supplyShip.setCrewCount(8);
        supplyShip.setCargoCapacity(3000);
        supplyShip.setStatus(Ship.ShipStatus.INCOMING);
        supplyShip.setArrivalTime(Instant.now().plus(30, ChronoUnit.MINUTES));
        shipRepository.save(supplyShip);
        
        Ship researchShip = new Ship();
        researchShip.setName("SRV Discovery");
        researchShip.setType(Ship.ShipType.RESEARCH);
        researchShip.setCrewCount(25);
        researchShip.setCargoCapacity(1000);
        researchShip.setStatus(Ship.ShipStatus.INCOMING);
        researchShip.setArrivalTime(Instant.now().plus(2, ChronoUnit.HOURS));
        shipRepository.save(researchShip);
        
        Ship militaryShip = new Ship();
        militaryShip.setName("UNS Defender");
        militaryShip.setType(Ship.ShipType.MILITARY);
        militaryShip.setCrewCount(150);
        militaryShip.setCargoCapacity(800);
        militaryShip.setStatus(Ship.ShipStatus.INCOMING);
        militaryShip.setArrivalTime(Instant.now().plus(4, ChronoUnit.HOURS));
        shipRepository.save(militaryShip);
        
        log.info("Created 5 ships (2 docked, 3 incoming)");
    }
    
    private void initializeDockingBays() {
        // Get the docked ships for assignment
        var dockedShips = shipRepository.findByStatus(Ship.ShipStatus.DOCKED);
        
        // Bay 1 - Occupied (by first docked ship)
        DockingBay bay1 = new DockingBay();
        bay1.setBayNumber(1);
        bay1.setStatus(DockingBay.BayStatus.OCCUPIED);
        bay1.setCapacity(6000);
        if (!dockedShips.isEmpty()) {
            bay1.setCurrentShipId(dockedShips.get(0).getId());
        }
        bayRepository.save(bay1);
        
        // Bay 2 - Occupied (by second docked ship)
        DockingBay bay2 = new DockingBay();
        bay2.setBayNumber(2);
        bay2.setStatus(DockingBay.BayStatus.OCCUPIED);
        bay2.setCapacity(4000);
        if (dockedShips.size() > 1) {
            bay2.setCurrentShipId(dockedShips.get(1).getId());
        }
        bayRepository.save(bay2);
        
        // Bay 3 - Reserved
        DockingBay bay3 = new DockingBay();
        bay3.setBayNumber(3);
        bay3.setStatus(DockingBay.BayStatus.RESERVED);
        bay3.setCapacity(5000);
        bayRepository.save(bay3);
        
        // Bay 4 - Available
        DockingBay bay4 = new DockingBay();
        bay4.setBayNumber(4);
        bay4.setStatus(DockingBay.BayStatus.AVAILABLE);
        bay4.setCapacity(3000);
        bayRepository.save(bay4);
        
        // Bay 5 - Available
        DockingBay bay5 = new DockingBay();
        bay5.setBayNumber(5);
        bay5.setStatus(DockingBay.BayStatus.AVAILABLE);
        bay5.setCapacity(5000);
        bayRepository.save(bay5);
        
        // Bay 6 - Available
        DockingBay bay6 = new DockingBay();
        bay6.setBayNumber(6);
        bay6.setStatus(DockingBay.BayStatus.AVAILABLE);
        bay6.setCapacity(8000);
        bayRepository.save(bay6);
        
        // Create docking logs for the occupied bays
        if (!dockedShips.isEmpty()) {
            DockingLog log1 = new DockingLog();
            log1.setShipId(dockedShips.get(0).getId());
            log1.setBayId(bay1.getId());
            log1.setAction(DockingLog.DockingAction.DOCK);
            logRepository.save(log1);
        }
        
        if (dockedShips.size() > 1) {
            DockingLog log2 = new DockingLog();
            log2.setShipId(dockedShips.get(1).getId());
            log2.setBayId(bay2.getId());
            log2.setAction(DockingLog.DockingAction.DOCK);
            logRepository.save(log2);
        }
        
        log.info("Created 6 docking bays (2 occupied, 1 reserved, 3 available)");
    }
}
