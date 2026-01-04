package com.nexus.inventory.repository;

import com.nexus.inventory.entity.Supply;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupplyRepository extends JpaRepository<Supply, Long> {

    List<Supply> findByCategory(Supply.SupplyCategory category);

    List<Supply> findBySectionId(Long sectionId);

    Optional<Supply> findByName(String name);

    /**
     * Find supply by ID with pessimistic write lock.
     * Prevents race conditions when multiple requests try to consume
     * the same supply simultaneously.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Supply s WHERE s.id = :id")
    Optional<Supply> findByIdWithLock(Long id);
    
    @Query("SELECT s FROM Supply s WHERE s.quantity <= s.minThreshold")
    List<Supply> findLowStockSupplies();
    
    @Query("SELECT COUNT(s) FROM Supply s WHERE s.quantity <= s.minThreshold")
    long countLowStockSupplies();
    
    boolean existsByName(String name);
}
