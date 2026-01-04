package com.nexus.docking.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "docking_bays")
public class DockingBay {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "bay_number", nullable = false, unique = true)
    private Integer bayNumber;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private BayStatus status;
    
    @Column(name = "current_ship_id", unique = true)
    private Long currentShipId;
    
    @Column(nullable = false)
    private Integer capacity;
    
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
    
    public Integer getBayNumber() {
        return bayNumber;
    }
    
    public void setBayNumber(Integer bayNumber) {
        this.bayNumber = bayNumber;
    }
    
    public BayStatus getStatus() {
        return status;
    }
    
    public void setStatus(BayStatus status) {
        this.status = status;
    }
    
    public Long getCurrentShipId() {
        return currentShipId;
    }
    
    public void setCurrentShipId(Long currentShipId) {
        this.currentShipId = currentShipId;
    }
    
    public Integer getCapacity() {
        return capacity;
    }
    
    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }
    
    public Instant getCreatedAt() {
        return createdAt;
    }
    
    public Instant getUpdatedAt() {
        return updatedAt;
    }
    
    public enum BayStatus {
        AVAILABLE,
        OCCUPIED,
        RESERVED,
        MAINTENANCE
    }
}
