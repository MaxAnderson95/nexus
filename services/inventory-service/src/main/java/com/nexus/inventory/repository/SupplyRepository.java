package com.nexus.inventory.repository;

import com.nexus.inventory.entity.Supply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupplyRepository extends JpaRepository<Supply, Long> {
    
    List<Supply> findByCategory(Supply.SupplyCategory category);
    
    List<Supply> findBySectionId(Long sectionId);
    
    Optional<Supply> findByName(String name);
    
    @Query("SELECT s FROM Supply s WHERE s.quantity <= s.minThreshold")
    List<Supply> findLowStockSupplies();
    
    @Query("SELECT COUNT(s) FROM Supply s WHERE s.quantity <= s.minThreshold")
    long countLowStockSupplies();
    
    boolean existsByName(String name);
}
