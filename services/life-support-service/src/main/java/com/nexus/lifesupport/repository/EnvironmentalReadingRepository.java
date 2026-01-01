package com.nexus.lifesupport.repository;

import com.nexus.lifesupport.entity.EnvironmentalReading;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnvironmentalReadingRepository extends JpaRepository<EnvironmentalReading, Long> {
    
    List<EnvironmentalReading> findBySectionId(Long sectionId);
    
    @Query("SELECT e FROM EnvironmentalReading e WHERE e.id IN " +
           "(SELECT MAX(e2.id) FROM EnvironmentalReading e2 GROUP BY e2.sectionId)")
    List<EnvironmentalReading> findLatestBySections();
    
    @Query("SELECT e FROM EnvironmentalReading e WHERE e.sectionId = :sectionId ORDER BY e.createdAt DESC LIMIT 1")
    Optional<EnvironmentalReading> findLatestBySectionId(Long sectionId);
}
