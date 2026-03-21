package com.lavela.pool.controller;

import com.lavela.pool.dto.request.CreatePaymentRequest;
import com.lavela.pool.dto.request.ZaloPayCallbackRequest;
import com.lavela.pool.dto.response.ApiResponse;
import com.lavela.pool.dto.response.PaymentResponse;
import com.lavela.pool.security.UserPrincipal;
import com.lavela.pool.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * BE2: Payment APIs.
 * - POST /payments: create payment intent, return redirectUrl (customer only).
 * - POST /webhooks/payment/ZALOPAY: called by ZaloPay (no auth); verify MAC + update booking/payment state.
 */
@Tag(name = "Payments", description = "Create payment and webhook (server-only)")
@Slf4j
@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * POST /payments
     * Create payment for a booking. Validates booking is PENDING_PAYMENT and not expired.
     * Returns redirectUrl (or SDK params) for client to redirect user to payment gateway.
     */
    @Operation(summary = "Create payment",
               description = "Creates a payment for the given booking. Returns redirectUrl for payment gateway. Booking must be PENDING_PAYMENT and not expired.")
    @PostMapping("/payments")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('USER','STAFF','ADMIN')")
    public ApiResponse<PaymentResponse> createPayment(
            @Valid @RequestBody CreatePaymentRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ApiResponse.ok(paymentService.createPayment(principal.getUserId(), request));
    }

    /**
     * POST /webhooks/payment/ZALOPAY
     * Server-to-server callback from ZaloPay (sandbox/prod).
     *
     * Spec payload:
     * { "data": "<json string>", "mac": "<hmac hex>", "type": 1 }
     *
     * Response must be:
     * { "return_code": 1, "return_message": "success" }
     */
    @Operation(
            summary = "ZaloPay webhook (server callback)",
            description = "ZaloPay callback endpoint. Verifies MAC (key2) and updates booking/payment state."
    )
    @PostMapping(value = "/webhooks/payment/ZALOPAY", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public java.util.Map<String, Object> zalopayWebhook(@RequestBody ZaloPayCallbackRequest callback) {
        try {
            paymentService.handleZaloPayWebhook(callback);
            return java.util.Map.of("return_code", 1, "return_message", "success");
        } catch (Throwable t) {
            // ZaloPay expects JSON body, not Spring's generic 500 — catch everything including Error.
            String msg = t.getMessage() == null ? "fail" : t.getMessage();
            if (msg.length() > 500) {
                msg = msg.substring(0, 500) + "...";
            }
            log.warn("ZaloPay webhook handler failed: {}", msg, t);
            return java.util.Map.of("return_code", 2, "return_message", msg);
        }
    }
}
