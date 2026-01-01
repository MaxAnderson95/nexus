package com.nexus.power.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "power_logs")
public class PowerLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PowerAction action;
    
    @Column(name = "amount_kw", nullable = false)
    private Double amountKw;
    
    @Column(name = "system_name")
    private String systemName;
    
    @Column(name = "source_id")
    private Long sourceId;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public PowerAction getAction() {
        return action;
    }
    
    public void setAction(PowerAction action) {
        this.action = action;
    }
    
    public Double getAmountKw() {
        return amountKw;
    }
    
    public void setAmountKw(Double amountKw) {
        this.amountKw = amountKw;
    }
    
    public String getSystemName() {
        return systemName;
    }
    
    public void setSystemName(String systemName) {
        this.systemName = systemName;
    }
    
    public Long getSourceId() {
        return sourceId;
    }
    
    public void setSourceId(Long sourceId) {
        this.sourceId = sourceId;
    }
    
    public Instant getCreatedAt() {
        return createdAt;
    }
    
    public enum PowerAction {
        ALLOCATE,
        DEALLOCATE,
        SOURCE_ONLINE,
        SOURCE_OFFLINE,
        OUTPUT_CHANGE
    }
}
