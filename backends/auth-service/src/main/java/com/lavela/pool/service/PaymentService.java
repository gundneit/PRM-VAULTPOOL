package com.lavela.pool.service;

import com.lavela.pool.domain.entity.*;
import com.lavela.pool.domain.enums.BookingStatus;
import com.lavela.pool.domain.enums.PaymentStatus;
import com.lavela.pool.dto.request.CreatePaymentRequest;
import com.lavela.pool.dto.request.ZaloPayCallbackRequest;
import com.lavela.pool.dto.response.PaymentResponse;
import com.lavela.pool.exception.BookingConflictException;
import com.lavela.pool.exception.ResourceNotFoundException;
import com.lavela.pool.integration.zalopay.ZaloPayClient;
import com.lavela.pool.integration.zalopay.ZaloPayCreateOrderResult;
import com.lavela.pool.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * BE2: Payment + ZaloPay callback handling.
 * - POST /payments: create ZaloPay order, return redirectUrl (zp_trans_token for SDK payOrder).
 * - POST /webhooks/payment/ZALOPAY: verify MAC, then confirm payment (no ZaloPay order-query).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private static final String STATUS_ACTIVE = "ACTIVE";
    private static final String STATUS_FULL = "FULL";

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final SlotRepository slotRepository;
    private final InventoryLogRepository inventoryLogRepository;
    private final ZaloPayClient zaloPayClient;

    /**
     * Create payment for a booking. Validates booking is PENDING_PAYMENT and not expired.
     * Returns redirectUrl (mock: link to fake payment page; production: provider URL).
     * One payment per booking (MVP); if payment already exists and is CREATED/PENDING, returns existing.
     */
    @Transactional
    public PaymentResponse createPayment(Long userId, CreatePaymentRequest req) {
        Booking booking = bookingRepository.findByIdAndUserId(req.getBookingId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + req.getBookingId()));

        if (booking.getStatus() != BookingStatus.PENDING_PAYMENT) {
            throw new BookingConflictException(
                    "Booking is not pending payment. Current status: " + booking.getStatus());
        }
        if (booking.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BookingConflictException("Booking has expired. Please create a new booking.");
        }

        // Idempotent: if payment already exists for this booking and still pending, return it
        var existing = paymentRepository.findByBookingId(booking.getId());
        if (existing.isPresent()) {
            Payment p = existing.get();
            if (p.getStatus() == PaymentStatus.CREATED || p.getStatus() == PaymentStatus.PENDING) {
                return toResponse(p);
            }
            throw new BookingConflictException("A payment already exists for this booking with status: " + p.getStatus());
        }

        String provider = normalizeProvider(req.getMethod());
        String providerTxnId;
        String redirectUrl;

        if ("ZALOPAY".equals(provider)) {
            long amountVnd = booking.getAmount().longValueExact();
            // app_user: use local userId to identify merchant user for the order
            String appUser = String.valueOf(userId);
            String appTransId = zaloPayClient.generateAppTransId();

            ZaloPayCreateOrderResult orderResult = zaloPayClient.createOrderWithAppTransId(amountVnd, appUser, appTransId);
            providerTxnId = appTransId;
            redirectUrl = orderResult.getZpTransToken();
        } else {
            throw new BookingConflictException("Unsupported payment method: " + provider + ". Only ZALOPAY is supported.");
        }

        Payment payment = Payment.builder()
                .booking(booking)
                .provider(provider)
                .providerTxnId(providerTxnId)
                .amount(booking.getAmount())
                .currency("VND")
                .status(PaymentStatus.PENDING)
                .redirectUrl(redirectUrl)
                .build();
        paymentRepository.save(payment);

        log.info("Payment created: paymentId={}, bookingId={}, provider={}, providerTxnId={}",
                payment.getId(), booking.getId(), provider, providerTxnId);

        return toResponse(payment);
    }

    /**
     * ZaloPay callback (App-to-App order callback, type=1).
     * Verifies MAC (key2), then confirms payment (no order-query).
     */
    @Transactional
    public void handleZaloPayWebhook(ZaloPayCallbackRequest callback) {
        String appTransId = zaloPayClient.verifyCallbackAndExtractAppTransId(callback);
        String normalizedProvider = "ZALOPAY";

        var paymentOpt = paymentRepository.findByProviderAndProviderTxnId(normalizedProvider, appTransId);
        if (paymentOpt.isEmpty()) {
            log.warn("ZaloPay webhook unknown app_trans_id={}", appTransId);
            return;
        }

        Payment payment = paymentOpt.get();

        // Idempotent: already terminal state
        if (payment.getStatus() == PaymentStatus.SUCCESS || payment.getStatus() == PaymentStatus.FAILED
                || payment.getStatus() == PaymentStatus.REFUNDED) {
            log.info("ZaloPay webhook idempotent: paymentId={}, status={}", payment.getId(), payment.getStatus());
            return;
        }

        Booking booking = bookingRepository.findByIdWithLock(payment.getBooking().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found for payment " + payment.getId()));

        log.info("ZaloPay webhook verified: paymentId={}, bookingId={}, app_trans_id={} — confirming without order-query",
                payment.getId(), booking.getId(), appTransId);
        applyPaymentSuccess(payment, booking);
    }

    private void applyPaymentSuccess(Payment payment, Booking booking) {
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setPaidAt(LocalDateTime.now());
        paymentRepository.save(payment);

        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setPaymentStatus("SUCCESS");
        bookingRepository.save(booking);

        Slot slot = booking.getSlot();
        inventoryLogRepository.save(InventoryLog.builder()
                .slot(slot)
                .booking(booking)
                .payment(payment)
                .delta(0)
                .capacityAfter(slot.getCapacityAvailable())
                .reason("CONFIRM")
                .actor("WEBHOOK")
                .build());

        log.info("Payment success: paymentId={}, bookingId={}, CONFIRMED", payment.getId(), booking.getId());
    }

    private String normalizeProvider(String method) {
        if (method == null || method.isBlank()) return "MOCK";
        return method.trim().toUpperCase();
    }

    /**
     * Staff/Admin: refund a successful payment. Booking -> CANCELED, release slot, inventory_log RELEASE.
     * Idempotent: if payment already REFUNDED, returns current state without error.
     */
    @Transactional
    public PaymentResponse refundPayment(Long paymentId, String staffFirebaseUid) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found: " + paymentId));

        if (payment.getStatus() == PaymentStatus.REFUNDED) {
            log.info("Refund idempotent: paymentId={}", paymentId);
            return toResponse(payment);
        }
        if (payment.getStatus() != PaymentStatus.SUCCESS) {
            throw new BookingConflictException(
                    "Only SUCCESS payments can be refunded. Current status: " + payment.getStatus());
        }

        Booking booking = bookingRepository.findByIdWithLock(payment.getBooking().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
        if (booking.getStatus() != BookingStatus.CONFIRMED && booking.getStatus() != BookingStatus.CHECKED_IN) {
            throw new BookingConflictException("Booking must be CONFIRMED or CHECKED_IN to refund. Current: " + booking.getStatus());
        }

        payment.setStatus(PaymentStatus.REFUNDED);
        payment.setRefundedAt(LocalDateTime.now());
        paymentRepository.save(payment);

        booking.setStatus(BookingStatus.CANCELED);
        booking.setPaymentStatus("REFUNDED");
        booking.setCancelReason("Refunded by staff");
        bookingRepository.save(booking);

        Slot slot = slotRepository.findByIdForUpdate(booking.getSlot().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Slot not found"));
        int released = booking.getQty();
        int newAvailable = slot.getCapacityAvailable() + released;
        slot.setCapacityAvailable(newAvailable);
        if (STATUS_FULL.equalsIgnoreCase(slot.getStatus()) && newAvailable > 0) {
            slot.setStatus(STATUS_ACTIVE);
        }
        slotRepository.save(slot);

        inventoryLogRepository.save(InventoryLog.builder()
                .slot(slot)
                .booking(booking)
                .payment(payment)
                .delta(released)
                .capacityAfter(newAvailable)
                .reason("RELEASE")
                .actor(staffFirebaseUid)
                .build());

        log.info("Refund completed: paymentId={}, bookingId={}, staff={}", paymentId, booking.getId(), staffFirebaseUid);
        return toResponse(payment);
    }

    private PaymentResponse toResponse(Payment p) {
        return PaymentResponse.builder()
                .id(p.getId())
                .bookingId(p.getBooking().getId())
                .provider(p.getProvider())
                .providerTxnId(p.getProviderTxnId())
                .amount(p.getAmount())
                .currency(p.getCurrency())
                .status(p.getStatus())
                .redirectUrl(p.getRedirectUrl())
                .paidAt(p.getPaidAt())
                .refundedAt(p.getRefundedAt())
                .createdAt(p.getCreatedAt())
                .build();
    }
}
