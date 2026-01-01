package com.nexus.inventory.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "cargo_items")
public class CargoItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manifest_id", nullable = false)
    @JsonIgnore
    private CargoManifest manifest;
    
    @Column(name = "supply_id")
    private Long supplyId;
    
    @Column(name = "supply_name", nullable = false)
    private String supplyName;
    
    @Column(nullable = false)
    private Integer quantity;
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public CargoManifest getManifest() {
        return manifest;
    }
    
    public void setManifest(CargoManifest manifest) {
        this.manifest = manifest;
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
}
