package com.lavela.pool.controller;

import com.lavela.pool.dto.response.ApiResponse;
import com.lavela.pool.dto.response.InventoryLogResponse;
import com.lavela.pool.service.InventoryLogService;
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
 * API tra cứu Inventory Log (Audit Trail) kho vé.
 * Dùng để debug overbooking và xử lý khiếu nại từ khách hàng.
 */
@Tag(name = "Inventory Logs", description = "Audit trail biến động kho vé — STAFF/ADMIN only")
@RestController
@RequestMapping("/inventory-logs")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class InventoryLogController {

    private final InventoryLogService inventoryLogService;

    /**
     * GET /inventory-logs/slot/{slotId}
     * Lấy toàn bộ lịch sử biến động capacity của một slot cụ thể.
     * Hữu ích khi điều tra overbooking ở slot.
     */
    @Operation(
        summary = "Audit log theo Slot",
        description = "Trả về toàn bộ history thay đổi capacity của slot, mới nhất trước. " +
                      "Dùng để debug overbooking."
    )
    @GetMapping("/slot/{slotId}")
    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    public ApiResponse<List<InventoryLogResponse>> getLogsBySlot(
            @Parameter(description = "ID của slot cần tra cứu")
            @PathVariable Long slotId
    ) {
        return ApiResponse.ok(inventoryLogService.getLogsBySlot(slotId));
    }

    /**
     * GET /inventory-logs/booking/{bookingCode}
     * Tra soát khiếu nại: staff nhận booking code VP-XXXXXXXX từ khách
     * → lấy toàn bộ lịch sử biến động liên quan đến booking đó.
     * Trả 404 nếu không tồn tại log nào cho mã này.
     */
    @Operation(
        summary = "Audit log theo Booking Code (tra soát khiếu nại)",
        description = "Nhận mã booking VP-XXXXXXXX từ khách hàng, trả về lịch sử biến động " +
                      "kho vé liên quan. Thứ tự từ cũ đến mới để dễ đọc flow. " +
                      "404 nếu không tìm thấy log nào cho booking code này."
    )
    @GetMapping("/booking/{bookingCode}")
    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    public ApiResponse<List<InventoryLogResponse>> getLogsByBookingCode(
            @Parameter(description = "Booking code dạng VP-XXXXXXXX")
            @PathVariable String bookingCode
    ) {
        return ApiResponse.ok(inventoryLogService.getLogsByBookingCode(bookingCode));
    }

    /**
     * GET /inventory-logs?from=YYYY-MM-DD&to=YYYY-MM-DD
     * Lấy toàn bộ audit log của hệ thống trong khoảng ngày chỉ định.
     * Chỉ ADMIN mới được gọi — dữ liệu toàn hệ thống nhạy cảm.
     */
    @Operation(
        summary = "Audit log toàn hệ thống theo khoảng ngày",
        description = "Trả về tất cả inventory log trong khoảng [from, to]. " +
                      "Chỉ dành cho ADMIN. Mới nhất trước."
    )
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<InventoryLogResponse>> getLogsByDateRange(
            @Parameter(description = "Ngày bắt đầu (yyyy-MM-dd)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,

            @Parameter(description = "Ngày kết thúc (yyyy-MM-dd, inclusive)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return ApiResponse.ok(inventoryLogService.getLogsByDateRange(from, to));
    }
}
