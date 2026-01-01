package com.nexus.power.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "power_allocations")
public class PowerAllocation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "system_name", nullable = false)
    private String systemName;
    
    @Column(name = "section_id")
    private Long sectionId;
    
    @Column(name = "allocated_kw", nullable = false)
    private Double allocatedKw;
    
    @Column(nullable = false)
    private Integer priority;
    
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
    
    public String getSystemName() {
        return systemName;
    }
    
    public void setSystemName(String systemName) {
        this.systemName = systemName;
    }
    
    public Long getSectionId() {
        return sectionId;
    }
    
    public void setSectionId(Long sectionId) {
        this.sectionId = sectionId;
    }
    
    public Double getAllocatedKw() {
        return allocatedKw;
    }
    
    public void setAllocatedKw(Double allocatedKw) {
        this.allocatedKw = allocatedKw;
    }
    
    public Integer getPriority() {
        return priority;
    }
    
    public void setPriority(Integer priority) {
        this.priority = priority;
    }
    
    public Instant getCreatedAt() {
        return createdAt;
    }
    
    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
