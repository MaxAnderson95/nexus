package com.nexus.crew.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "crew_members")
public class CrewMember {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false)
    private String rank;
    
    @Column(nullable = false)
    private String role;
    
    @Column(name = "section_id")
    private Long sectionId;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private CrewStatus status;
    
    @Column(name = "arrived_at")
    private Instant arrivedAt;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
        if (arrivedAt == null) {
            arrivedAt = Instant.now();
        }
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
    
    public String getRank() {
        return rank;
    }
    
    public void setRank(String rank) {
        this.rank = rank;
    }
    
    public String getRole() {
        return role;
    }
    
    public void setRole(String role) {
        this.role = role;
    }
    
    public Long getSectionId() {
        return sectionId;
    }
    
    public void setSectionId(Long sectionId) {
        this.sectionId = sectionId;
    }
    
    public CrewStatus getStatus() {
        return status;
    }
    
    public void setStatus(CrewStatus status) {
        this.status = status;
    }
    
    public Instant getArrivedAt() {
        return arrivedAt;
    }
    
    public void setArrivedAt(Instant arrivedAt) {
        this.arrivedAt = arrivedAt;
    }
    
    public Instant getCreatedAt() {
        return createdAt;
    }
    
    public Instant getUpdatedAt() {
        return updatedAt;
    }
    
    public enum CrewStatus {
        ACTIVE,
        ON_LEAVE,
        OFF_DUTY,
        IN_TRANSIT
    }
}
