package com.nexus.power.repository;

import com.nexus.power.entity.PowerSource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PowerSourceRepository extends JpaRepository<PowerSource, Long> {
    
    List<PowerSource> findByStatus(PowerSource.PowerSourceStatus status);
    
    List<PowerSource> findByType(PowerSource.PowerSourceType type);
    
    @Query("SELECT SUM(ps.maxOutputKw) FROM PowerSource ps WHERE ps.status = 'ONLINE'")
    Double getTotalMaxOutput();
    
    @Query("SELECT SUM(ps.currentOutputKw) FROM PowerSource ps WHERE ps.status = 'ONLINE'")
    Double getTotalCurrentOutput();
    
    boolean existsByName(String name);
}
