package com.lavela.pool.controller;

import com.lavela.pool.dto.response.ApiResponse;
import com.lavela.pool.dto.response.PaymentResponse;
import com.lavela.pool.security.UserPrincipal;
import com.lavela.pool.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * BE2: Staff/Admin payment operations — refund, reconciliation.
 */
@Tag(name = "Staff Payments", description = "Refund and reconciliation for STAFF/ADMIN")
@RestController
@RequestMapping("/api/staff/payments")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('STAFF','ADMIN')")
@SecurityRequirement(name = "bearerAuth")
public class StaffPaymentController {

    private final PaymentService paymentService;

    /**
     * POST /api/staff/payments/{id}/refund
     * Refund a successful payment. Booking -> CANCELED, slot capacity released, inventory_log RELEASE.
     * Idempotent if already refunded.
     */
    @Operation(summary = "Refund payment",
               description = "Refunds a SUCCESS payment. Booking set to CANCELED, slot capacity released. Idempotent.")
    @PostMapping("/{id}/refund")
    public ApiResponse<PaymentResponse> refund(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ApiResponse.ok(paymentService.refundPayment(id, principal.getFirebaseUid()));
    }
}
