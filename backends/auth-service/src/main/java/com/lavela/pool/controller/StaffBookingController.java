package com.lavela.pool.controller;

import com.lavela.pool.dto.response.ApiResponse;
import com.lavela.pool.dto.response.BookingResponse;
import com.lavela.pool.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "Staff Bookings", description = "Staff/Admin booking management")
@RestController
@RequestMapping("/api/staff/bookings")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('STAFF','ADMIN')")
@SecurityRequirement(name = "bearerAuth")
public class StaffBookingController {

    private final BookingService bookingService;

    /**
     * GET /api/staff/bookings?slotId=1001
     *   → Tất cả booking của slot đó (mọi ngày)
     *
     * GET /api/staff/bookings?date=2026-03-16
     *   → Tất cả booking của các slot có start_time trong ngày đó
     *
     * GET /api/staff/bookings
     *   → Mặc định: booking của ngày hôm nay
     *
     * Dùng để staff biết ai đã đặt slot nào, phục vụ check-in tại quầy.
     */
    @Operation(summary = "List bookings by slot or date",
               description = "Staff view: filter by slotId OR date (YYYY-MM-DD). Defaults to today if no filter given.")
    @GetMapping
    public ApiResponse<List<BookingResponse>> getBookings(
            @RequestParam(required = false) Long slotId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return ApiResponse.ok(bookingService.getStaffBookings(slotId, date));
    }
}
