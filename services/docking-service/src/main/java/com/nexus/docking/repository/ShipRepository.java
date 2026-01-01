package com.nexus.docking.repository;

import com.nexus.docking.entity.Ship;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShipRepository extends JpaRepository<Ship, Long> {
    
    List<Ship> findByStatus(Ship.ShipStatus status);
    
    List<Ship> findByType(Ship.ShipType type);
    
    List<Ship> findByStatusIn(List<Ship.ShipStatus> statuses);
    
    List<Ship> findByStatusOrderByArrivalTimeAsc(Ship.ShipStatus status);
    
    boolean existsByName(String name);
    
    long countByStatus(Ship.ShipStatus status);
}
