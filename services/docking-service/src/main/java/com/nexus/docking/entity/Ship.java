package com.nexus.docking.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "ships")
public class Ship {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ShipType type;
    
    @Column(name = "crew_count", nullable = false)
    private Integer crewCount;
    
    @Column(name = "cargo_capacity", nullable = false)
    private Integer cargoCapacity;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ShipStatus status;
    
    @Column(name = "arrival_time")
    private Instant arrivalTime;
    
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
    
    public ShipType getType() {
        return type;
    }
    
    public void setType(ShipType type) {
        this.type = type;
    }
    
    public Integer getCrewCount() {
        return crewCount;
    }
    
    public void setCrewCount(Integer crewCount) {
        this.crewCount = crewCount;
    }
    
    public Integer getCargoCapacity() {
        return cargoCapacity;
    }
    
    public void setCargoCapacity(Integer cargoCapacity) {
        this.cargoCapacity = cargoCapacity;
    }
    
    public ShipStatus getStatus() {
        return status;
    }
    
    public void setStatus(ShipStatus status) {
        this.status = status;
    }
    
    public Instant getArrivalTime() {
        return arrivalTime;
    }
    
    public void setArrivalTime(Instant arrivalTime) {
        this.arrivalTime = arrivalTime;
    }
    
    public Instant getCreatedAt() {
        return createdAt;
    }
    
    public Instant getUpdatedAt() {
        return updatedAt;
    }
    
    public enum ShipType {
        CARGO,
        PASSENGER,
        MILITARY,
        RESEARCH,
        SUPPLY
    }
    
    public enum ShipStatus {
        INCOMING,
        DOCKED,
        DEPARTING,
        IN_TRANSIT
    }
}
