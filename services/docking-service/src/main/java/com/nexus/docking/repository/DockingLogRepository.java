package com.nexus.docking.repository;

import com.nexus.docking.entity.DockingLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface DockingLogRepository extends JpaRepository<DockingLog, Long> {
    
    List<DockingLog> findByShipIdOrderByTimestampDesc(Long shipId);
    
    List<DockingLog> findByBayIdOrderByTimestampDesc(Long bayId);
    
    List<DockingLog> findByAction(DockingLog.DockingAction action);
    
    List<DockingLog> findAllByOrderByTimestampDesc();
    
    List<DockingLog> findByTimestampAfterOrderByTimestampDesc(Instant since);
}
