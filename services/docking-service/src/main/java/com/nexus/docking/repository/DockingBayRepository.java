package com.nexus.docking.repository;

import com.nexus.docking.entity.DockingBay;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
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

    /**
     * Find an available bay with pessimistic write lock to prevent race conditions
     * when multiple ships try to dock simultaneously.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM DockingBay b WHERE b.status = :status ORDER BY b.bayNumber ASC LIMIT 1")
    Optional<DockingBay> findFirstAvailableBayWithLock(DockingBay.BayStatus status);

    boolean existsByBayNumber(Integer bayNumber);

    long countByStatus(DockingBay.BayStatus status);
}
