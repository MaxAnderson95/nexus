package com.nexus.crew.repository;

import com.nexus.crew.entity.Section;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SectionRepository extends JpaRepository<Section, Long> {

    Optional<Section> findByName(String name);

    /**
     * Find section by ID with pessimistic write lock.
     * Prevents race conditions when multiple requests try to update
     * section occupancy simultaneously.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Section s WHERE s.id = :id")
    Optional<Section> findByIdWithLock(Long id);

    List<Section> findByDeck(Integer deck);
    
    @Query("SELECT SUM(s.currentOccupancy) FROM Section s")
    Integer getTotalOccupancy();
    
    @Query("SELECT SUM(s.maxCapacity) FROM Section s")
    Integer getTotalCapacity();
    
    boolean existsByName(String name);
}
