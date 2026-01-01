package com.nexus.docking.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "docking_logs")
public class DockingLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "ship_id", nullable = false)
    private Long shipId;
    
    @Column(name = "bay_id")
    private Long bayId;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private DockingAction action;
    
    @Column(name = "timestamp", nullable = false, updatable = false)
    private Instant timestamp;
    
    @PrePersist
    protected void onCreate() {
        timestamp = Instant.now();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getShipId() {
        return shipId;
    }
    
    public void setShipId(Long shipId) {
        this.shipId = shipId;
    }
    
    public Long getBayId() {
        return bayId;
    }
    
    public void setBayId(Long bayId) {
        this.bayId = bayId;
    }
    
    public DockingAction getAction() {
        return action;
    }
    
    public void setAction(DockingAction action) {
        this.action = action;
    }
    
    public Instant getTimestamp() {
        return timestamp;
    }
    
    public enum DockingAction {
        DOCK,
        UNDOCK,
        ARRIVAL_SCHEDULED,
        DEPARTURE_SCHEDULED
    }
}
