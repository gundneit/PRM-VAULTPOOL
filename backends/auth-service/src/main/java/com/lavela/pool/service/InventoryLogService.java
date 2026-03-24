package com.lavela.pool.service;

import com.lavela.pool.dto.response.InventoryLogResponse;
import com.lavela.pool.exception.ResourceNotFoundException;
import com.lavela.pool.repository.InventoryLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Dịch vụ tra cứu Inventory Log (Audit Trail) cho kho vé.
 * Phục vụ STAFF/ADMIN khi cần xử lý khiếu nại hoặc kiểm tra bất thường.
 */
@Service
@RequiredArgsConstructor
public class InventoryLogService {

    private final InventoryLogRepository inventoryLogRepository;

    /**
     * Lấy toàn bộ audit trail của một slot.
     * Dùng khi cần phân tích biến động capacity của slot cụ thể.
     *
     * @param slotId ID của slot cần tra cứu
     */
    @Transactional(readOnly = true)
    public List<InventoryLogResponse> getLogsBySlot(Long slotId) {
        return inventoryLogRepository.findBySlotIdOrderByCreatedAtDesc(slotId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Tra soát khiếu nại theo booking code (VP-XXXXXXXX).
     * Staff nhận mã từ khách → lấy toàn bộ lịch sử biến động liên quan.
     * Trả lỗi 404 nếu không tìm thấy booking code nào.
     *
     * @param bookingCode Mã booking dạng VP-XXXXXXXX
     */
    @Transactional(readOnly = true)
    public List<InventoryLogResponse> getLogsByBookingCode(String bookingCode) {
        List<InventoryLogResponse> logs =
                inventoryLogRepository.findByBookingBookingCodeOrderByCreatedAtAsc(bookingCode)
                        .stream()
                        .map(this::toResponse)
                        .toList();

        if (logs.isEmpty()) {
            throw new ResourceNotFoundException(
                    "No inventory logs found for booking code: " + bookingCode);
        }
        return logs;
    }

    /**
     * Lấy tất cả audit log trong khoảng thời gian.
     * Dành cho ADMIN để giám sát toàn hệ thống.
     *
     * @param from ngày bắt đầu (inclusive)
     * @param to   ngày kết thúc (inclusive)
     */
    @Transactional(readOnly = true)
    public List<InventoryLogResponse> getLogsByDateRange(LocalDate from, LocalDate to) {
        LocalDateTime dtFrom = from.atStartOfDay();
        LocalDateTime dtTo   = to.plusDays(1).atStartOfDay();
        return inventoryLogRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(dtFrom, dtTo)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private InventoryLogResponse toResponse(com.lavela.pool.domain.entity.InventoryLog log) {
        return InventoryLogResponse.builder()
                .id(log.getId())
                .slotId(log.getSlot() != null ? log.getSlot().getId() : null)
                .bookingCode(log.getBooking() != null ? log.getBooking().getBookingCode() : null)
                .paymentId(log.getPayment() != null ? log.getPayment().getId() : null)
                .delta(log.getDelta())
                .capacityAfter(log.getCapacityAfter())
                .reason(log.getReason())
                .actor(log.getActor())
                .createdAt(log.getCreatedAt())
                .build();
    }
}
