package com.nexus.power.repository;

import com.nexus.power.entity.PowerAllocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PowerAllocationRepository extends JpaRepository<PowerAllocation, Long> {
    
    Optional<PowerAllocation> findBySystemName(String systemName);
    
    List<PowerAllocation> findBySystemNameContainingIgnoreCase(String systemName);
    
    List<PowerAllocation> findBySectionId(Long sectionId);
    
    @Query("SELECT SUM(pa.allocatedKw) FROM PowerAllocation pa")
    Double getTotalAllocated();
    
    @Query("SELECT pa FROM PowerAllocation pa ORDER BY pa.priority ASC")
    List<PowerAllocation> findAllOrderByPriority();
    
    boolean existsBySystemName(String systemName);
}
