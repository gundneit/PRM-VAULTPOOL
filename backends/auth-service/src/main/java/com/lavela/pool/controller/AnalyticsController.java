package com.lavela.pool.controller;

import com.lavela.pool.dto.response.ApiResponse;
import com.lavela.pool.dto.response.RevenueStatsResponse;
import com.lavela.pool.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * API thống kê doanh thu và số vé bán theo thời gian thực.
 * Chỉ dành cho STAFF và ADMIN.
 */
@Tag(name = "Analytics", description = "Thống kê doanh thu và số vé bán — STAFF/ADMIN only")
@RestController
@RequestMapping("/analytics")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    /**
     * GET /analytics/revenue
     * Thống kê tổng doanh thu và số vé bán theo khoảng thời gian.
     * Granularity: DAY (mặc định) | WEEK | MONTH
     *
     * Ví dụ: GET /analytics/revenue?from=2025-01-01&to=2025-03-31&granularity=MONTH
     */
    @Operation(
        summary = "Thống kê doanh thu theo thời gian",
        description = "Trả về tổng doanh thu và số vé bán, group theo DAY/WEEK/MONTH trong khoảng [from, to]. " +
                      "Chỉ tính booking CONFIRMED và CHECKED_IN."
    )
    @GetMapping("/revenue")
    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    public ApiResponse<List<RevenueStatsResponse>> getRevenueSummary(
            @Parameter(description = "Ngày bắt đầu (yyyy-MM-dd)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,

            @Parameter(description = "Ngày kết thúc (yyyy-MM-dd, inclusive)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,

            @Parameter(description = "Granularity: DAY | WEEK | MONTH (mặc định: DAY)")
            @RequestParam(defaultValue = "DAY") String granularity
    ) {
        return ApiResponse.ok(analyticsService.getRevenueSummary(from, to, granularity));
    }

    /**
     * GET /analytics/revenue/by-pool
     * Thống kê doanh thu phân tách theo từng hồ bơi.
     *
     * Ví dụ: GET /analytics/revenue/by-pool?from=2025-01-01&to=2025-12-31
     */
    @Operation(
        summary = "Thống kê doanh thu theo hồ bơi",
        description = "Trả về doanh thu và số vé, phân tách theo từng pool. " +
                      "Sắp xếp giảm dần theo tổng doanh thu."
    )
    @GetMapping("/revenue/by-pool")
    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    public ApiResponse<List<RevenueStatsResponse>> getRevenueByPool(
            @Parameter(description = "Ngày bắt đầu (yyyy-MM-dd)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,

            @Parameter(description = "Ngày kết thúc (yyyy-MM-dd, inclusive)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return ApiResponse.ok(analyticsService.getRevenueByPool(from, to));
    }
}
