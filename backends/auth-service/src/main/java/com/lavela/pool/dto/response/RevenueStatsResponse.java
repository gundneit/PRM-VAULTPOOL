package com.lavela.pool.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * Thống kê doanh thu và số vé bán theo một khoảng thời gian.
 * Dùng cho dashboard STAFF/ADMIN.
 */
@Getter
@Builder
public class RevenueStatsResponse {

    /**
     * Nhãn thời gian của kỳ thống kê.
     * - Granularity DAY   → "2025-03-24"
     * - Granularity WEEK  → "2025-W12"
     * - Granularity MONTH → "2025-03"
     * - Khi group by pool → tên pool
     */
    private String period;

    /** Tổng doanh thu (chỉ tính booking CONFIRMED hoặc CHECKED_IN) */
    private BigDecimal totalRevenue;

    /** Tổng số vé đã bán (sum of qty) */
    private Integer ticketsSold;

    /** Số lượng booking */
    private Long bookingCount;

    /**
     * ID hồ bơi — chỉ có giá trị khi group by pool, null khi group by time.
     */
    private Long poolId;

    /** Tên hồ bơi — chỉ có giá trị khi group by pool */
    private String poolName;
}
