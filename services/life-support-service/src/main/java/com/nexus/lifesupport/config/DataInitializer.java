package com.nexus.lifesupport.config;

import com.nexus.lifesupport.entity.Alert;
import com.nexus.lifesupport.entity.EnvironmentalReading;
import com.nexus.lifesupport.entity.EnvironmentalSettings;
import com.nexus.lifesupport.repository.AlertRepository;
import com.nexus.lifesupport.repository.EnvironmentalReadingRepository;
import com.nexus.lifesupport.repository.EnvironmentalSettingsRepository;
import jakarta.persistence.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@Component
public class DataInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);
    private static final String INIT_LOCK_KEY = "init:lock:life-support";
    private static final Duration LOCK_TTL = Duration.ofMinutes(5);
    private static final Duration LOCK_WAIT_TIMEOUT = Duration.ofSeconds(30);

    private final EnvironmentalSettingsRepository settingsRepository;
    private final EnvironmentalReadingRepository readingRepository;
    private final AlertRepository alertRepository;
    private final EntityManager entityManager;
    private final RedisTemplate<String, String> redisTemplate;

    public DataInitializer(
            EnvironmentalSettingsRepository settingsRepository,
            EnvironmentalReadingRepository readingRepository,
            AlertRepository alertRepository,
            EntityManager entityManager,
            RedisTemplate<String, String> redisTemplate) {
        this.settingsRepository = settingsRepository;
        this.readingRepository = readingRepository;
        this.alertRepository = alertRepository;
        this.entityManager = entityManager;
        this.redisTemplate = redisTemplate;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        log.info("Checking demo data for Life Support Service...");

        RedisDistributedLock lock = new RedisDistributedLock(redisTemplate, INIT_LOCK_KEY, LOCK_TTL);

        if (!lock.tryAcquire()) {
            log.info("Another instance is initializing data, waiting for completion...");
            waitForInitCompletion(lock);
            log.info("Life Support Service demo data check complete (initialized by another instance)");
            return;
        }

        try {
            if (settingsRepository.count() == 0) {
                log.info("Initializing environmental settings...");
                initializeSettings();
                initializeReadings();
                initializeAlerts();
            } else {
                log.info("Environmental data already exists, skipping initialization");
            }
        } finally {
            lock.release();
        }

        log.info("Life Support Service demo data check complete");
    }

    private void waitForInitCompletion(RedisDistributedLock lock) {
        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < LOCK_WAIT_TIMEOUT.toMillis()) {
            try {
                Thread.sleep(500);
                if (!lock.isLocked()) {
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
        log.info("Resetting Life Support Service tables...");
        
        // Delete all data using batch delete (single SQL DELETE statement)
        alertRepository.deleteAllInBatch();
        readingRepository.deleteAllInBatch();
        settingsRepository.deleteAllInBatch();
        
        // Flush to ensure deletes are committed before inserts
        entityManager.flush();
        entityManager.clear();
        
        // Re-initialize demo data
        initializeSettings();
        initializeReadings();
        initializeAlerts();
        
        log.info("Life Support Service tables reset complete");
    }
    
    private void initializeSettings() {
        // All sections have very high max occupancy to avoid capacity issues in normal operations
        createSection(1L, "Bridge", 21.0, 22.0, 101.3, 45.0, 5000, 8);
        createSection(2L, "Engineering", 21.0, 22.0, 101.3, 40.0, 5000, 12);
        createSection(3L, "Habitation Deck A", 21.0, 22.0, 101.3, 50.0, 10000, 35);
        createSection(4L, "Habitation Deck B", 21.0, 22.0, 101.3, 50.0, 10000, 28);
        createSection(5L, "Medical Bay", 22.0, 21.0, 101.3, 55.0, 5000, 5);
        createSection(6L, "Science Lab", 21.0, 20.0, 101.3, 40.0, 5000, 10);
        createSection(7L, "Cargo Hold", 21.0, 20.0, 101.3, 40.0, 5000, 2);
        createSection(8L, "Maintenance Bay", 21.0, 22.0, 101.3, 40.0, 5000, 6);
        
        log.info("Created 8 section settings");
    }
    
    private void createSection(Long id, String name, Double targetO2, Double targetTemp,
                               Double targetPressure, Double targetHumidity, 
                               Integer maxOccupancy, Integer currentOccupancy) {
        EnvironmentalSettings settings = new EnvironmentalSettings();
        settings.setSectionId(id);
        settings.setSectionName(name);
        settings.setTargetO2(targetO2);
        settings.setTargetTemperature(targetTemp);
        settings.setTargetPressure(targetPressure);
        settings.setTargetHumidity(targetHumidity);
        settings.setMaxOccupancy(maxOccupancy);
        settings.setCurrentOccupancy(currentOccupancy);
        settingsRepository.save(settings);
    }
    
    private void initializeReadings() {
        // Create initial readings for each section - all nominal/healthy values
        createReading(1L, "Bridge", 21.0, 0.04, 22.0, 101.3, 45.0);
        createReading(2L, "Engineering", 21.0, 0.04, 22.0, 101.3, 40.0);
        createReading(3L, "Habitation Deck A", 21.0, 0.04, 22.0, 101.3, 50.0);
        createReading(4L, "Habitation Deck B", 21.0, 0.04, 22.0, 101.3, 50.0);
        createReading(5L, "Medical Bay", 22.0, 0.03, 21.0, 101.3, 55.0);
        createReading(6L, "Science Lab", 21.0, 0.04, 20.0, 101.3, 40.0);
        createReading(7L, "Cargo Hold", 21.0, 0.04, 20.0, 101.3, 40.0);
        createReading(8L, "Maintenance Bay", 21.0, 0.04, 22.0, 101.3, 40.0);
        
        log.info("Created 8 environmental readings");
    }
    
    private void createReading(Long sectionId, String sectionName, Double o2, Double co2,
                               Double temp, Double pressure, Double humidity) {
        EnvironmentalReading reading = new EnvironmentalReading();
        reading.setSectionId(sectionId);
        reading.setSectionName(sectionName);
        reading.setO2Level(o2);
        reading.setCo2Level(co2);
        reading.setTemperature(temp);
        reading.setPressure(pressure);
        reading.setHumidity(humidity);
        readingRepository.save(reading);
    }
    
    private void initializeAlerts() {
        // No initial alerts - system starts in healthy state for demo
        log.info("No initial alerts - system is nominal");
    }
    
    private void createAlert(Long sectionId, String sectionName, Alert.AlertType type,
                            Alert.AlertSeverity severity, String message) {
        Alert alert = new Alert();
        alert.setSectionId(sectionId);
        alert.setSectionName(sectionName);
        alert.setType(type);
        alert.setSeverity(severity);
        alert.setMessage(message);
        alert.setAcknowledged(false);
        alertRepository.save(alert);
    }
}
