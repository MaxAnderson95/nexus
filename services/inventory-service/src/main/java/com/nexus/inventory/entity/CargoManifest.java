package com.nexus.inventory.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cargo_manifests")
public class CargoManifest {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "ship_id")
    private Long shipId;
    
    @Column(name = "ship_name")
    private String shipName;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ManifestStatus status;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    
    @Column(name = "completed_at")
    private Instant completedAt;
    
    @OneToMany(mappedBy = "manifest", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<CargoItem> items = new ArrayList<>();
    
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
    
    public Long getShipId() {
        return shipId;
    }
    
    public void setShipId(Long shipId) {
        this.shipId = shipId;
    }
    
    public String getShipName() {
        return shipName;
    }
    
    public void setShipName(String shipName) {
        this.shipName = shipName;
    }
    
    public ManifestStatus getStatus() {
        return status;
    }
    
    public void setStatus(ManifestStatus status) {
        this.status = status;
    }
    
    public Instant getCreatedAt() {
        return createdAt;
    }
    
    public Instant getCompletedAt() {
        return completedAt;
    }
    
    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }
    
    public List<CargoItem> getItems() {
        return items;
    }
    
    public void setItems(List<CargoItem> items) {
        this.items = items;
    }
    
    public void addItem(CargoItem item) {
        items.add(item);
        item.setManifest(this);
    }
    
    public enum ManifestStatus {
        PENDING,
        UNLOADING,
        COMPLETED,
        CANCELLED
    }
}
