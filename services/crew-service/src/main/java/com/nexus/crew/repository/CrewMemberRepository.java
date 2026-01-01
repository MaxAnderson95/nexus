package com.nexus.crew.repository;

import com.nexus.crew.entity.CrewMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CrewMemberRepository extends JpaRepository<CrewMember, Long> {
    
    List<CrewMember> findBySectionId(Long sectionId);
    
    List<CrewMember> findByStatus(CrewMember.CrewStatus status);
    
    List<CrewMember> findByRank(String rank);
    
    List<CrewMember> findByRole(String role);
    
    @Query("SELECT COUNT(cm) FROM CrewMember cm WHERE cm.status = 'ACTIVE'")
    Long countActive();
    
    @Query("SELECT COUNT(cm) FROM CrewMember cm WHERE cm.sectionId = :sectionId")
    Integer countBySectionId(Long sectionId);
    
    boolean existsByName(String name);
}
