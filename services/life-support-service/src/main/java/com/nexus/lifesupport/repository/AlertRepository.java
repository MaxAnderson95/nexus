package com.nexus.lifesupport.repository;

import com.nexus.lifesupport.entity.Alert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {
    
    List<Alert> findByAcknowledgedFalse();
    
    List<Alert> findByAcknowledgedFalseOrderBySeverityDescCreatedAtDesc();
    
    List<Alert> findBySectionId(Long sectionId);
    
    List<Alert> findBySectionIdAndAcknowledgedFalse(Long sectionId);
    
    List<Alert> findBySeverity(Alert.AlertSeverity severity);
    
    long countByAcknowledgedFalse();
}
