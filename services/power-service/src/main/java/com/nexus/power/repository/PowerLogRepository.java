package com.nexus.power.repository;

import com.nexus.power.entity.PowerLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface PowerLogRepository extends JpaRepository<PowerLog, Long> {
    
    List<PowerLog> findByAction(PowerLog.PowerAction action);
    
    List<PowerLog> findBySourceId(Long sourceId);
    
    List<PowerLog> findBySystemName(String systemName);
    
    Page<PowerLog> findAllByOrderByCreatedAtDesc(Pageable pageable);
    
    List<PowerLog> findByCreatedAtAfter(Instant after);
}
