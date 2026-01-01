package com.nexus.lifesupport.repository;

import com.nexus.lifesupport.entity.EnvironmentalSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EnvironmentalSettingsRepository extends JpaRepository<EnvironmentalSettings, Long> {
    
    Optional<EnvironmentalSettings> findBySectionId(Long sectionId);
    
    boolean existsBySectionId(Long sectionId);
}
