package com.nexus.crew.repository;

import com.nexus.crew.entity.CrewAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CrewAssignmentRepository extends JpaRepository<CrewAssignment, Long> {
    
    List<CrewAssignment> findByCrewId(Long crewId);
    
    List<CrewAssignment> findByStatus(CrewAssignment.AssignmentStatus status);
    
    @Query("SELECT ca FROM CrewAssignment ca WHERE ca.crewId = :crewId AND ca.status = 'IN_PROGRESS'")
    List<CrewAssignment> findActiveByCrewId(Long crewId);
    
    @Query("SELECT ca FROM CrewAssignment ca ORDER BY ca.startTime DESC")
    List<CrewAssignment> findAllOrderByStartTimeDesc();
}
