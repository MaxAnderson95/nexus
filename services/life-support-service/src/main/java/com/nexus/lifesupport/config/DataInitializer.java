package com.nexus.lifesupport.config;

import com.nexus.lifesupport.entity.Alert;
import com.nexus.lifesupport.entity.EnvironmentalReading;
import com.nexus.lifesupport.entity.EnvironmentalSettings;
import com.nexus.lifesupport.repository.AlertRepository;
import com.nexus.lifesupport.repository.EnvironmentalReadingRepository;
import com.nexus.lifesupport.repository.EnvironmentalSettingsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DataInitializer implements ApplicationRunner {
    
    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);
    
    private final EnvironmentalSettingsRepository settingsRepository;
    private final EnvironmentalReadingRepository readingRepository;
    private final AlertRepository alertRepository;
    
    public DataInitializer(
            EnvironmentalSettingsRepository settingsRepository,
            EnvironmentalReadingRepository readingRepository,
            AlertRepository alertRepository) {
        this.settingsRepository = settingsRepository;
        this.readingRepository = readingRepository;
        this.alertRepository = alertRepository;
    }
    
    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        log.info("Checking demo data for Life Support Service...");
        
        if (settingsRepository.count() == 0) {
            log.info("Initializing environmental settings...");
            initializeSettings();
            initializeReadings();
            initializeAlerts();
        } else {
            log.info("Environmental data already exists, skipping initialization");
        }
        
        log.info("Life Support Service demo data check complete");
    }
    
    private void initializeSettings() {
        createSection(1L, "Bridge", 21.0, 22.0, 101.3, 45.0, 15, 8);
        createSection(2L, "Engineering", 21.0, 24.0, 101.3, 40.0, 25, 12);
        createSection(3L, "Habitation Deck A", 21.0, 22.0, 101.3, 50.0, 50, 35);
        createSection(4L, "Habitation Deck B", 21.0, 22.0, 101.3, 50.0, 50, 28);
        createSection(5L, "Medical Bay", 22.0, 21.0, 101.3, 55.0, 20, 5);
        createSection(6L, "Science Lab", 21.0, 20.0, 101.3, 40.0, 15, 10);
        createSection(7L, "Cargo Hold", 20.0, 18.0, 100.0, 35.0, 10, 2);
        createSection(8L, "Maintenance Bay", 20.5, 23.0, 101.3, 40.0, 12, 6);
        
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
        // Create initial readings for each section with slight variations
        createReading(1L, "Bridge", 20.9, 0.04, 22.1, 101.2, 44.5);
        createReading(2L, "Engineering", 20.8, 0.05, 24.5, 101.1, 38.0);
        createReading(3L, "Habitation Deck A", 21.0, 0.04, 22.0, 101.3, 50.0);
        createReading(4L, "Habitation Deck B", 21.1, 0.04, 21.8, 101.4, 51.0);
        createReading(5L, "Medical Bay", 22.0, 0.03, 21.0, 101.3, 55.0);
        createReading(6L, "Science Lab", 20.5, 0.04, 20.2, 101.2, 40.0);
        createReading(7L, "Cargo Hold", 19.8, 0.05, 17.5, 99.8, 33.0);
        createReading(8L, "Maintenance Bay", 20.3, 0.05, 23.5, 101.0, 42.0);
        
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
        // Low O2 warning in Cargo Hold
        createAlert(7L, "Cargo Hold", Alert.AlertType.O2_LOW, Alert.AlertSeverity.WARNING,
                "O2 levels below optimal in Cargo Hold (19.8%)");
        
        // Temperature warning in Engineering
        createAlert(2L, "Engineering", Alert.AlertType.TEMPERATURE_HIGH, Alert.AlertSeverity.WARNING,
                "Temperature elevated in Engineering section (24.5C)");
        
        // Low pressure warning in Cargo Hold
        createAlert(7L, "Cargo Hold", Alert.AlertType.PRESSURE_LOW, Alert.AlertSeverity.WARNING,
                "Pressure below nominal in Cargo Hold (99.8 kPa)");
        
        // Info about maintenance
        createAlert(8L, "Maintenance Bay", Alert.AlertType.SYSTEM_MALFUNCTION, Alert.AlertSeverity.INFO,
                "Scheduled maintenance for air recycler in Maintenance Bay");
        
        // CO2 elevated in Engineering
        createAlert(2L, "Engineering", Alert.AlertType.CO2_HIGH, Alert.AlertSeverity.INFO,
                "CO2 slightly elevated in Engineering (0.05%)");
        
        log.info("Created 5 alerts");
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
