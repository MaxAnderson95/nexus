package com.nexus.inventory.repository;

import com.nexus.inventory.entity.CargoManifest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CargoManifestRepository extends JpaRepository<CargoManifest, Long> {
    
    List<CargoManifest> findByStatus(CargoManifest.ManifestStatus status);
    
    List<CargoManifest> findByShipId(Long shipId);
    
    @Query("SELECT m FROM CargoManifest m ORDER BY m.createdAt DESC")
    List<CargoManifest> findAllOrderByCreatedAtDesc();
    
    long countByStatus(CargoManifest.ManifestStatus status);
}
