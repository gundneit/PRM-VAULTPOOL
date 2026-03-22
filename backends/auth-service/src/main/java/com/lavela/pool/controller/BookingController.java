package com.lavela.pool.controller;

import com.lavela.pool.dto.request.CheckInRequest;
import com.lavela.pool.dto.request.CreateBookingRequest;
import com.lavela.pool.dto.response.ApiResponse;
import com.lavela.pool.dto.response.BookingResponse;
import com.lavela.pool.security.UserPrincipal;
import com.lavela.pool.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Bookings", description = "Booking lifecycle APIs")
@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class BookingController {

    private final BookingService bookingService;

    /**
     * POST /bookings
     * Customer tạo booking mới. Yêu cầu slot còn chỗ.
     * Anti-overbooking: dùng pessimistic lock trên slot.
     */
    @Operation(summary = "Create a booking",
               description = "Creates a booking for a slot. Returns 409 if slot is full or unavailable.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<BookingResponse> createBooking(
            @Valid @RequestBody CreateBookingRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ApiResponse.ok(
                bookingService.createBooking(principal.getUserId(), principal.getFirebaseUid(), request)
        );
    }

    /**
     * GET /bookings?me=true         → danh sách booking của user đang đăng nhập
     * GET /bookings?userId={userId} → (STAFF/ADMIN) xem booking của user khác
     *
     * MVP: chỉ hỗ trợ ?me=true. STAFF xem theo userId sẽ làm ở BE2.
     */
    @Operation(summary = "Get my bookings",
               description = "Returns bookings of the authenticated user, newest first.")
    @GetMapping
    @PreAuthorize("hasAnyRole('USER','STAFF','ADMIN')")
    public ApiResponse<List<BookingResponse>> getMyBookings(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ApiResponse.ok(bookingService.getMyBookings(principal.getUserId()));
    }

    /**
     * GET /bookings/cart
     * Lấy danh sách booking đang trong giỏ hàng (IN_CART) của user.
     */
    @Operation(summary = "Get my cart",
               description = "Returns bookings with IN_CART status for the authenticated user.")
    @GetMapping("/cart")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<BookingResponse>> getCart(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ApiResponse.ok(bookingService.getCart(principal.getUserId()));
    }

    /**
     * POST /bookings/{id}/checkout
     * Checkout 1 booking từ giỏ hàng (IN_CART) sang PENDING_PAYMENT.
     */
    @Operation(summary = "Checkout a cart booking",
               description = "Transitions a specific IN_CART booking to PENDING_PAYMENT and reserves slot capacity.")
    @PostMapping("/{id}/checkout")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<BookingResponse> checkoutBooking(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ApiResponse.ok(bookingService.checkoutBooking(id, principal.getUserId(), principal.getFirebaseUid()));
    }

    /**
     * GET /bookings/{id}
     * Chỉ user sở hữu booking mới xem được. Trả 404 nếu không tìm thấy hoặc không phải của mình.
     */
    @Operation(summary = "Get booking detail",
               description = "Returns detail of a specific booking. Only accessible by the booking owner.")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER','STAFF','ADMIN')")
    public ApiResponse<BookingResponse> getBookingById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ApiResponse.ok(bookingService.getBookingById(id, principal.getUserId()));
    }

    /**
     * POST /bookings/{id}/checkin
     * Staff scan QR code → verify booking_code hash → chuyển CONFIRMED → CHECKED_IN.
     * Idempotent: gọi nhiều lần cũng trả 200.
     * Chỉ STAFF và ADMIN mới gọi được endpoint này.
     */
    @Operation(summary = "Staff check-in via QR",
               description = "Validates the QR hash and transitions booking to CHECKED_IN. Idempotent.")
    @PostMapping("/{id}/checkin")
    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    public ApiResponse<BookingResponse> checkIn(
            @PathVariable Long id,
            @Valid @RequestBody CheckInRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ApiResponse.ok(
                bookingService.checkIn(id, request.getHash(), principal.getFirebaseUid())
        );
    }

    /**
     * POST /bookings/{id}/cancel
     * User huỷ booking của mình.
     * Chỉ được huỷ khi status PENDING_PAYMENT hoặc CONFIRMED.
     * Release capacity về slot, ghi inventory_logs CANCEL.
     */
    @Operation(summary = "Cancel a booking",
               description = "Cancels own booking. Only allowed when status is PENDING_PAYMENT or CONFIRMED. Returns 409 otherwise.")
    @PostMapping("/{id}/cancel")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<BookingResponse> cancelBooking(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ApiResponse.ok(
                bookingService.cancelBooking(id, principal.getUserId(), principal.getFirebaseUid())
        );
    }
}
