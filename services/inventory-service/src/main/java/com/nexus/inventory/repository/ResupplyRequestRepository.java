package com.nexus.inventory.repository;

import com.nexus.inventory.entity.ResupplyRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResupplyRequestRepository extends JpaRepository<ResupplyRequest, Long> {
    
    List<ResupplyRequest> findByStatus(ResupplyRequest.RequestStatus status);
    
    List<ResupplyRequest> findBySupplyId(Long supplyId);
    
    @Query("SELECT r FROM ResupplyRequest r ORDER BY r.requestedAt DESC")
    List<ResupplyRequest> findAllOrderByRequestedAtDesc();
    
    long countByStatus(ResupplyRequest.RequestStatus status);
}
