package com.lavela.pool.repository;

import com.lavela.pool.domain.entity.InventoryLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface InventoryLogRepository extends JpaRepository<InventoryLog, Long> {

    /**
     * Lấy audit log của một slot, mới nhất trước — debug overbooking
     */
    List<InventoryLog> findBySlotIdOrderByCreatedAtDesc(Long slotId);

    /**
     * Lấy audit log theo slot + khoảng thời gian — dùng cho báo cáo
     */
    List<InventoryLog> findBySlotIdAndCreatedAtBetweenOrderByCreatedAtDesc(
            Long slotId, LocalDateTime from, LocalDateTime to);

    /**
     * Tra soát khiếu nại theo booking code (VP-XXXXXXXX).
     * Staff nhận mã từ khách → tìm toàn bộ biến động liên quan đến booking đó.
     */
    List<InventoryLog> findByBookingBookingCodeOrderByCreatedAtAsc(String bookingCode);

    /**
     * Lấy toàn bộ audit log trong một khoảng thời gian — dành cho ADMIN
     */
    List<InventoryLog> findByCreatedAtBetweenOrderByCreatedAtDesc(
            LocalDateTime from, LocalDateTime to);
}
