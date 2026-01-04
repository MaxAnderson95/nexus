package com.nexus.lifesupport.repository;

import com.nexus.lifesupport.entity.EnvironmentalSettings;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EnvironmentalSettingsRepository extends JpaRepository<EnvironmentalSettings, Long> {

    Optional<EnvironmentalSettings> findBySectionId(Long sectionId);

    /**
     * Find settings by section ID with pessimistic write lock.
     * Prevents race conditions when multiple requests try to update
     * occupancy simultaneously.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT es FROM EnvironmentalSettings es WHERE es.sectionId = :sectionId")
    Optional<EnvironmentalSettings> findBySectionIdWithLock(Long sectionId);

    boolean existsBySectionId(Long sectionId);
}
