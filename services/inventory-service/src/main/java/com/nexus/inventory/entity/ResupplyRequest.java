package com.nexus.inventory.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "resupply_requests")
public class ResupplyRequest {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "supply_id", nullable = false)
    private Long supplyId;
    
    @Column(name = "supply_name", nullable = false)
    private String supplyName;
    
    @Column(nullable = false)
    private Integer quantity;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private RequestStatus status;
    
    @Column(name = "requested_at", nullable = false, updatable = false)
    private Instant requestedAt;
    
    @Column(name = "completed_at")
    private Instant completedAt;
    
    @PrePersist
    protected void onCreate() {
        requestedAt = Instant.now();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getSupplyId() {
        return supplyId;
    }
    
    public void setSupplyId(Long supplyId) {
        this.supplyId = supplyId;
    }
    
    public String getSupplyName() {
        return supplyName;
    }
    
    public void setSupplyName(String supplyName) {
        this.supplyName = supplyName;
    }
    
    public Integer getQuantity() {
        return quantity;
    }
    
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
    
    public RequestStatus getStatus() {
        return status;
    }
    
    public void setStatus(RequestStatus status) {
        this.status = status;
    }
    
    public Instant getRequestedAt() {
        return requestedAt;
    }
    
    public Instant getCompletedAt() {
        return completedAt;
    }
    
    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }
    
    public enum RequestStatus {
        PENDING,
        APPROVED,
        IN_TRANSIT,
        DELIVERED,
        CANCELLED
    }
}
