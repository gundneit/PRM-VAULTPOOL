package com.lavela.pool.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * Phản hồi một bản ghi audit trail của kho vé.
 * Dùng cho tra soát khiếu nại và debug overbooking.
 */
@Getter
@Builder
public class InventoryLogResponse {

    private Long id;

    /** ID của slot liên quan */
    private Long slotId;

    /**
     * Booking code (VP-XXXXXXXX) — hiển thị thay vì internal bookingId
     * để staff có thể đối chiếu trực tiếp với khiếu nại từ khách.
     */
    private String bookingCode;

    /** ID của payment nếu có (nullable) */
    private Long paymentId;

    /**
     * Âm = giữ chỗ (RESERVE), Dương = trả chỗ (RELEASE/EXPIRE/CANCEL)
     */
    private Integer delta;

    /** Snapshot capacity_available ngay sau khi thay đổi */
    private Integer capacityAfter;

    /**
     * Lý do thay đổi: RESERVE | RELEASE | CONFIRM | CANCEL | EXPIRE | MANUAL_ADJUST
     */
    private String reason;

    /**
     * firebase_uid của user/staff thực hiện, hoặc "SYSTEM" cho scheduled jobs
     */
    private String actor;

    private LocalDateTime createdAt;
}
