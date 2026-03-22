package com.lavela.pool.service;

import com.lavela.pool.domain.entity.*;
import com.lavela.pool.domain.enums.BookingStatus;
import com.lavela.pool.dto.request.CreateBookingRequest;
import com.lavela.pool.dto.response.BookingResponse;
import com.lavela.pool.exception.BookingConflictException;
import com.lavela.pool.exception.ResourceNotFoundException;
import com.lavela.pool.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingService {

    private static final int BOOKING_TTL_MINUTES = 10;
    private static final String STATUS_ACTIVE = "ACTIVE";
    private static final String STATUS_FULL   = "FULL";

    private final BookingRepository      bookingRepository;
    private final SlotRepository         slotRepository;
    private final UserRepository         userRepository;
    private final InventoryLogRepository inventoryLogRepository;

    // =========================================================================
    // CREATE BOOKING
    // =========================================================================

    /**
     * Tạo booking mới với pessimistic lock trên slot để tránh overbooking.
     * Flow:
     *  1. Khoá slot (SELECT ... FOR UPDATE)
     *  2. Validate capacity và status
     *  3. Giảm capacity_available, cập nhật status slot nếu đầy
     *  4. Tạo booking (PENDING_PAYMENT, expires trong 10 phút)
     *  5. Ghi inventory_log RESERVE
     */
    @Transactional
    public BookingResponse createBooking(Long userId, String firebaseUid, CreateBookingRequest req) {
        // 1. Load user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // 2. Khoá slot bằng pessimistic write lock
        Slot slot = slotRepository.findByIdForUpdate(req.getSlotId())
                .orElseThrow(() -> new ResourceNotFoundException("Slot not found: " + req.getSlotId()));

        // 3. Validate slot
        if (slot.getStartTime().isBefore(LocalDateTime.now())) {
            throw new BookingConflictException("Cannot book a slot that has already started or passed.");
        }
        if (!STATUS_ACTIVE.equalsIgnoreCase(slot.getStatus())) {
            throw new BookingConflictException("Slot is not available (status: " + slot.getStatus() + ")");
        }
        boolean isCart = req.getStatus() == BookingStatus.IN_CART;
        BookingStatus statusToSave = req.getStatus() != null ? req.getStatus() : BookingStatus.PENDING_PAYMENT;
        int newAvailable = slot.getCapacityAvailable();

        if (!isCart) {
            if (slot.getCapacityAvailable() < req.getQty()) {
                throw new BookingConflictException(
                        "Not enough capacity. Available: " + slot.getCapacityAvailable() + ", requested: " + req.getQty()
                );
            }

            // 4. Giảm capacity
            newAvailable = slot.getCapacityAvailable() - req.getQty();
            slot.setCapacityAvailable(newAvailable);
            if (newAvailable == 0) {
                slot.setStatus(STATUS_FULL);
            }
            slotRepository.save(slot);
        }

        // 5. Tạo booking
        String bookingCode = generateBookingCode();
        Booking booking = Booking.builder()
                .user(user)
                .pool(slot.getPool())
                .slot(slot)
                .qty(req.getQty())
                .amount(slot.getPrice().multiply(java.math.BigDecimal.valueOf(req.getQty())))
                .status(statusToSave)
                .bookingCode(bookingCode)
                .expiresAt(isCart ? LocalDateTime.now().plusDays(7) : LocalDateTime.now().plusMinutes(BOOKING_TTL_MINUTES))
                .build();
        bookingRepository.save(booking);

        // 6. Ghi audit log
        if (!isCart) {
            saveInventoryLog(slot, booking, -req.getQty(), newAvailable, "RESERVE", firebaseUid);
        }

        log.info("Booking created: code={}, slotId={}, userId={}, qty={}, newAvailable={}",
                bookingCode, slot.getId(), userId, req.getQty(), newAvailable);

        return toResponse(booking);
    }

    // =========================================================================
    // QUERY
    // =========================================================================

    /**
     * Lấy danh sách booking của user, mới nhất trước.
     * User thường → chỉ xem của mình.
     * Staff/Admin → dùng getAllBySlot hoặc endpoint riêng (thuộc BE2).
     */
    @Transactional(readOnly = true)
    public List<BookingResponse> getMyBookings(Long userId) {
        return bookingRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Chi tiết booking — user chỉ xem được của mình (userId check),
     * staff không dùng endpoint này (họ dùng endpoint riêng ở BE2).
     */
    @Transactional(readOnly = true)
    public BookingResponse getBookingById(Long bookingId, Long userId) {
        Booking booking = bookingRepository.findByIdAndUserId(bookingId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + bookingId));
        return toResponse(booking);
    }

    // =========================================================================
    // CANCEL BOOKING
    // =========================================================================

    /**
     * User huỷ booking của mình.
     * Chỉ cho phép huỷ khi status là PENDING_PAYMENT hoặc CONFIRMED.
     * Release capacity về slot, ghi inventory_logs CANCEL.
     */
    @Transactional
    public BookingResponse cancelBooking(Long bookingId, Long userId, String firebaseUid) {
        // Chỉ tìm booking của đúng user
        Booking booking = bookingRepository.findByIdAndUserId(bookingId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + bookingId));

        // Chỉ được cancel khi PENDING_PAYMENT hoặc CONFIRMED
        if (booking.getStatus() != BookingStatus.PENDING_PAYMENT
                && booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new BookingConflictException(
                    "Cannot cancel booking with status: " + booking.getStatus()
                    + ". Only PENDING_PAYMENT or CONFIRMED bookings can be cancelled."
            );
        }

        // Lock slot trước khi trả capacity
        Slot slot = slotRepository.findByIdForUpdate(booking.getSlot().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Slot not found"));

        // Đặt booking thành CANCELED
        booking.setStatus(BookingStatus.CANCELED);
        booking.setCancelReason("Cancelled by user");
        bookingRepository.save(booking);

        // Trả lại capacity cho slot
        int released     = booking.getQty();
        int newAvailable = slot.getCapacityAvailable() + released;
        slot.setCapacityAvailable(newAvailable);

        // Nếu slot đang FULL và giờ có chỗ trống → ACTIVE lại
        if (STATUS_FULL.equalsIgnoreCase(slot.getStatus()) && newAvailable > 0) {
            slot.setStatus(STATUS_ACTIVE);
        }
        slotRepository.save(slot);

        // Ghi audit log
        saveInventoryLog(slot, booking, +released, newAvailable, "CANCEL", firebaseUid);

        log.info("Booking cancelled: bookingId={}, code={}, userId={}, released={}, newAvailable={}",
                bookingId, booking.getBookingCode(), userId, released, newAvailable);

        return toResponse(booking);
    }

    // =========================================================================
    // STAFF — XEM BOOKING THEO SLOT / NGÀY
    // =========================================================================

    /**
     * Staff xem danh sách booking để phục vụ check-in.
     * Lọc theo slotId và/hoặc date (YYYY-MM-DD).
     * Nếu không truyền filter → trả tất cả booking của ngày hôm nay.
     */
    @Transactional(readOnly = true)
    public List<BookingResponse> getStaffBookings(Long slotId, java.time.LocalDate date) {
        List<Booking> bookings;

        if (slotId != null) {
            // Lọc theo slot cụ thể
            bookings = bookingRepository.findBySlotIdOrderByCreatedAtDesc(slotId);
        } else {
            // Lọc theo ngày (dùng ngày hôm nay nếu không truyền)
            java.time.LocalDate target = date != null ? date : java.time.LocalDate.now();
            java.time.LocalDateTime start = target.atStartOfDay();
            java.time.LocalDateTime end   = target.plusDays(1).atStartOfDay();
            bookings = bookingRepository.findBySlotStartTimeBetweenOrderByCreatedAtDesc(start, end);
        }

        return bookings.stream().map(this::toResponse).toList();
    }

    // =========================================================================
    // CHECK-IN (STAFF)
    // =========================================================================

    /**
     * Staff scan QR → server verify hash == booking_code → chuyển CONFIRMED → CHECKED_IN.
     * Idempotent: nếu đã CHECKED_IN rồi thì return 200 luôn, không lỗi.
     */
    @Transactional
    public BookingResponse checkIn(Long bookingId, String hash, String staffFirebaseUid) {
        // Khoá pessimistic để tránh concurrent check-in
        Booking booking = bookingRepository.findByIdWithLock(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + bookingId));

        // Idempotent — đã check-in rồi thì không làm gì thêm
        if (booking.getStatus() == BookingStatus.CHECKED_IN) {
            log.info("Check-in idempotent: bookingId={}, staff={}", bookingId, staffFirebaseUid);
            return toResponse(booking);
        }

        // Verify hash (MVP: so sánh với booking_code)
        if (!booking.getBookingCode().equalsIgnoreCase(hash.trim())) {
            throw new BookingConflictException("Invalid QR hash");
        }

        // Chỉ cho phép check-in khi CONFIRMED
        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new BookingConflictException(
                    "Cannot check in — booking status is: " + booking.getStatus()
                    + ". Booking must be CONFIRMED first."
            );
        }

        // Chuyển trạng thái
        booking.setStatus(BookingStatus.CHECKED_IN);
        bookingRepository.save(booking);

        // Audit log — delta=0 vì capacity không thay đổi ở bước này
        saveInventoryLog(
                booking.getSlot(), booking,
                0, booking.getSlot().getCapacityAvailable(),
                "CONFIRM", staffFirebaseUid
        );

        log.info("Check-in successful: bookingId={}, code={}, staff={}",
                bookingId, booking.getBookingCode(), staffFirebaseUid);

        return toResponse(booking);
    }

    // =========================================================================
    // TTL EXPIRATION SCHEDULER
    // =========================================================================

    /**
     * Chạy mỗi 60 giây — tìm các booking PENDING_PAYMENT đã hết hạn,
     * chuyển sang EXPIRED và release capacity về slot.
     */
    @Scheduled(fixedDelay = 60_000)
    @Transactional
    public void expireStaleBookings() {
        List<Booking> stale = bookingRepository.findByStatusAndExpiresAtBefore(
                BookingStatus.PENDING_PAYMENT, LocalDateTime.now()
        );

        if (stale.isEmpty()) return;

        log.info("Expiring {} stale booking(s)", stale.size());

        for (Booking booking : stale) {
            try {
                expireSingleBooking(booking);
            } catch (Exception e) {
                // Không để 1 booking lỗi làm fail cả batch
                log.error("Failed to expire bookingId={}: {}", booking.getId(), e.getMessage(), e);
            }
        }

        // ============================================
        // CLEAN UP EXPIRED CART ITEMS
        // Xóa những item đang nằm trong giỏ nhưng Slot của hồ bơi đã bắt đầu/qua giờ.
        // ============================================
        List<Booking> staleCarts = bookingRepository.findByStatusAndSlotStartTimeBefore(
                BookingStatus.IN_CART, LocalDateTime.now()
        );
        if (!staleCarts.isEmpty()) {
            log.info("Deleting {} expired cart booking(s)", staleCarts.size());
            bookingRepository.deleteAllInBatch(staleCarts);
        }
    }

    private void expireSingleBooking(Booking booking) {
        // Khoá slot trước khi trả capacity
        Slot slot = slotRepository.findByIdForUpdate(booking.getSlot().getId())
                .orElse(null);

        booking.setStatus(BookingStatus.EXPIRED);
        bookingRepository.save(booking);

        if (slot != null) {
            int released    = booking.getQty();
            int newAvailable = slot.getCapacityAvailable() + released;
            slot.setCapacityAvailable(newAvailable);

            // Nếu slot đang FULL và giờ có chỗ trống → ACTIVE lại
            if (STATUS_FULL.equalsIgnoreCase(slot.getStatus()) && newAvailable > 0) {
                slot.setStatus(STATUS_ACTIVE);
            }
            slotRepository.save(slot);

            saveInventoryLog(slot, booking, +released, newAvailable, "EXPIRE", "SYSTEM");

            log.info("Expired bookingId={}, released {} slots back to slotId={}, newAvailable={}",
                    booking.getId(), released, slot.getId(), newAvailable);
        } else {
            log.warn("Slot not found for expired bookingId={}", booking.getId());
        }
    }

    // =========================================================================
    // HELPERS
    // =========================================================================

    private void saveInventoryLog(Slot slot, Booking booking, int delta, int capacityAfter,
                                   String reason, String actor) {
        inventoryLogRepository.save(InventoryLog.builder()
                .slot(slot)
                .booking(booking)
                .delta(delta)
                .capacityAfter(capacityAfter)
                .reason(reason)
                .actor(actor)
                .build());
    }

    private String generateBookingCode() {
        // Format: VP-<8 ký tự uppercase hex> — ví dụ: VP-3A7F092B
        return "VP-" + UUID.randomUUID().toString()
                .replace("-", "")
                .substring(0, 8)
                .toUpperCase();
    }

    private BookingResponse toResponse(Booking b) {
        Slot slot = b.getSlot();
        Pool pool = b.getPool();
        return BookingResponse.builder()
                .id(b.getId())
                .bookingCode(b.getBookingCode())
                .slotId(slot.getId())
                .poolId(pool.getId())
                .poolName(pool.getName())
                .poolAddress(pool.getAddress())
                .startTime(slot.getStartTime())
                .endTime(slot.getEndTime())
                .qty(b.getQty())
                .amount(b.getAmount())
                .status(b.getStatus())
                .paymentStatus(b.getPaymentStatus())
                .cancelReason(b.getCancelReason())
                .expiresAt(b.getExpiresAt())
                .createdAt(b.getCreatedAt())
                .build();
    }
}
