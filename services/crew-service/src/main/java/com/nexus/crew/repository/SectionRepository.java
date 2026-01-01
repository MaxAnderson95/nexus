package com.nexus.crew.repository;

import com.nexus.crew.entity.Section;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SectionRepository extends JpaRepository<Section, Long> {
    
    Optional<Section> findByName(String name);
    
    List<Section> findByDeck(Integer deck);
    
    @Query("SELECT SUM(s.currentOccupancy) FROM Section s")
    Integer getTotalOccupancy();
    
    @Query("SELECT SUM(s.maxCapacity) FROM Section s")
    Integer getTotalCapacity();
    
    boolean existsByName(String name);
}
