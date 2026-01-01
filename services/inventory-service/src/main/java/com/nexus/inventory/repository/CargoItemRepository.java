package com.nexus.inventory.repository;

import com.nexus.inventory.entity.CargoItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CargoItemRepository extends JpaRepository<CargoItem, Long> {
    
    List<CargoItem> findByManifestId(Long manifestId);
    
    List<CargoItem> findBySupplyId(Long supplyId);
}
