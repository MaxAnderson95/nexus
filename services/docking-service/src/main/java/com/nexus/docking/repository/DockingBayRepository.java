package com.nexus.docking.repository;

import com.nexus.docking.entity.DockingBay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DockingBayRepository extends JpaRepository<DockingBay, Long> {
    
    List<DockingBay> findAllByOrderByBayNumberAsc();
    
    List<DockingBay> findByStatus(DockingBay.BayStatus status);
    
    Optional<DockingBay> findByBayNumber(Integer bayNumber);
    
    Optional<DockingBay> findByCurrentShipId(Long shipId);
    
    Optional<DockingBay> findFirstByStatusOrderByBayNumberAsc(DockingBay.BayStatus status);
    
    boolean existsByBayNumber(Integer bayNumber);
    
    long countByStatus(DockingBay.BayStatus status);
}
