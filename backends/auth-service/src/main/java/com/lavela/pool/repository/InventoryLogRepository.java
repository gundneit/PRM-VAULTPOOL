package com.lavela.pool.repository;

import com.lavela.pool.domain.entity.InventoryLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventoryLogRepository extends JpaRepository<InventoryLog, Long> {

    /**
     * Lấy audit log của một slot, mới nhất trước — debug overbooking
     */
    List<InventoryLog> findBySlotIdOrderByCreatedAtDesc(Long slotId);
}
