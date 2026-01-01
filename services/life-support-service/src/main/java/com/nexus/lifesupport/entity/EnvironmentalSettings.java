package com.nexus.lifesupport.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "environmental_settings")
public class EnvironmentalSettings {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "section_id", nullable = false, unique = true)
    private Long sectionId;
    
    @Column(name = "section_name", nullable = false)
    private String sectionName;
    
    @Column(name = "target_o2", nullable = false)
    private Double targetO2;
    
    @Column(name = "target_temperature", nullable = false)
    private Double targetTemperature;
    
    @Column(name = "target_pressure", nullable = false)
    private Double targetPressure;
    
    @Column(name = "target_humidity", nullable = false)
    private Double targetHumidity;
    
    @Column(name = "max_occupancy", nullable = false)
    private Integer maxOccupancy;
    
    @Column(name = "current_occupancy", nullable = false)
    private Integer currentOccupancy;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getSectionId() { return sectionId; }
    public void setSectionId(Long sectionId) { this.sectionId = sectionId; }
    
    public String getSectionName() { return sectionName; }
    public void setSectionName(String sectionName) { this.sectionName = sectionName; }
    
    public Double getTargetO2() { return targetO2; }
    public void setTargetO2(Double targetO2) { this.targetO2 = targetO2; }
    
    public Double getTargetTemperature() { return targetTemperature; }
    public void setTargetTemperature(Double targetTemperature) { this.targetTemperature = targetTemperature; }
    
    public Double getTargetPressure() { return targetPressure; }
    public void setTargetPressure(Double targetPressure) { this.targetPressure = targetPressure; }
    
    public Double getTargetHumidity() { return targetHumidity; }
    public void setTargetHumidity(Double targetHumidity) { this.targetHumidity = targetHumidity; }
    
    public Integer getMaxOccupancy() { return maxOccupancy; }
    public void setMaxOccupancy(Integer maxOccupancy) { this.maxOccupancy = maxOccupancy; }
    
    public Integer getCurrentOccupancy() { return currentOccupancy; }
    public void setCurrentOccupancy(Integer currentOccupancy) { this.currentOccupancy = currentOccupancy; }
    
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
