package com.nexus.lifesupport.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "environmental_readings")
public class EnvironmentalReading {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "section_id", nullable = false)
    private Long sectionId;
    
    @Column(name = "section_name", nullable = false)
    private String sectionName;
    
    @Column(name = "o2_level", nullable = false)
    private Double o2Level;
    
    @Column(name = "co2_level", nullable = false)
    private Double co2Level;
    
    @Column(nullable = false)
    private Double temperature;
    
    @Column(nullable = false)
    private Double pressure;
    
    @Column(nullable = false)
    private Double humidity;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getSectionId() { return sectionId; }
    public void setSectionId(Long sectionId) { this.sectionId = sectionId; }
    
    public String getSectionName() { return sectionName; }
    public void setSectionName(String sectionName) { this.sectionName = sectionName; }
    
    public Double getO2Level() { return o2Level; }
    public void setO2Level(Double o2Level) { this.o2Level = o2Level; }
    
    public Double getCo2Level() { return co2Level; }
    public void setCo2Level(Double co2Level) { this.co2Level = co2Level; }
    
    public Double getTemperature() { return temperature; }
    public void setTemperature(Double temperature) { this.temperature = temperature; }
    
    public Double getPressure() { return pressure; }
    public void setPressure(Double pressure) { this.pressure = pressure; }
    
    public Double getHumidity() { return humidity; }
    public void setHumidity(Double humidity) { this.humidity = humidity; }
    
    public Instant getCreatedAt() { return createdAt; }
}
