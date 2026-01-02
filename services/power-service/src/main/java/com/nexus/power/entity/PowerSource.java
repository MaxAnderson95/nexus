package com.nexus.power.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "power_sources")
public class PowerSource {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PowerSourceType type;
    
    @Column(name = "max_output_kw", nullable = false)
    private Double maxOutputKw;
    
    @Column(name = "current_output_kw", nullable = false)
    private Double currentOutputKw;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PowerSourceStatus status;
    
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
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public PowerSourceType getType() {
        return type;
    }
    
    public void setType(PowerSourceType type) {
        this.type = type;
    }
    
    public Double getMaxOutputKw() {
        return maxOutputKw;
    }
    
    public void setMaxOutputKw(Double maxOutputKw) {
        this.maxOutputKw = maxOutputKw;
    }
    
    public Double getCurrentOutputKw() {
        return currentOutputKw;
    }
    
    public void setCurrentOutputKw(Double currentOutputKw) {
        this.currentOutputKw = currentOutputKw;
    }
    
    public PowerSourceStatus getStatus() {
        return status;
    }
    
    public void setStatus(PowerSourceStatus status) {
        this.status = status;
    }
    
    public Instant getCreatedAt() {
        return createdAt;
    }
    
    public Instant getUpdatedAt() {
        return updatedAt;
    }
    
    public enum PowerSourceType {
        SOLAR_ARRAY,
        FUSION_REACTOR,
        BATTERY_BANK,
        FUEL_CELL
    }
    
    public enum PowerSourceStatus {
        ONLINE,
        OFFLINE,
        STANDBY,
        MAINTENANCE,
        DEGRADED
    }
}
