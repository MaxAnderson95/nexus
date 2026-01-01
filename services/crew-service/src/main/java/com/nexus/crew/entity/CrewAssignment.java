package com.nexus.crew.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "crew_assignments")
public class CrewAssignment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "crew_id", nullable = false)
    private Long crewId;
    
    @Column(nullable = false)
    private String task;
    
    @Column(name = "start_time", nullable = false)
    private Instant startTime;
    
    @Column(name = "end_time")
    private Instant endTime;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AssignmentStatus status;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
        if (status == null) {
            status = AssignmentStatus.PENDING;
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
    
    public Long getCrewId() {
        return crewId;
    }
    
    public void setCrewId(Long crewId) {
        this.crewId = crewId;
    }
    
    public String getTask() {
        return task;
    }
    
    public void setTask(String task) {
        this.task = task;
    }
    
    public Instant getStartTime() {
        return startTime;
    }
    
    public void setStartTime(Instant startTime) {
        this.startTime = startTime;
    }
    
    public Instant getEndTime() {
        return endTime;
    }
    
    public void setEndTime(Instant endTime) {
        this.endTime = endTime;
    }
    
    public AssignmentStatus getStatus() {
        return status;
    }
    
    public void setStatus(AssignmentStatus status) {
        this.status = status;
    }
    
    public Instant getCreatedAt() {
        return createdAt;
    }
    
    public Instant getUpdatedAt() {
        return updatedAt;
    }
    
    public enum AssignmentStatus {
        PENDING,
        IN_PROGRESS,
        COMPLETED,
        CANCELLED
    }
}
