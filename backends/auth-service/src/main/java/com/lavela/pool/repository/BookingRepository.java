package com.lavela.pool.repository;

import com.lavela.pool.domain.entity.Booking;
import com.lavela.pool.domain.enums.BookingStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    /**
     * Đếm số lượng Booking theo user và trạng thái (dùng cho Cart - chỉ tính những slot chưa bắt đầu)
     */
    int countByUserIdAndStatusAndSlotStartTimeAfter(Long userId, BookingStatus status, LocalDateTime time);

    /**
     * Tìm các Booking trong giỏ hàng (IN_CART) đã qua thời gian bắt đầu của slot
     */
    List<Booking> findByStatusAndSlotStartTimeBefore(BookingStatus status, LocalDateTime time);

    /**
     * Lấy danh sách booking của user, mới nhất trước
     */
    List<Booking> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * Lấy danh sách booking của user theo trạng thái (vd: IN_CART)
     */
    List<Booking> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, BookingStatus status);

    /**
     * Lấy booking theo id + userId — đảm bảo user chỉ xem của mình
     */
    Optional<Booking> findByIdAndUserId(Long id, Long userId);

    /**
     * Cho scheduler: lấy tất cả PENDING_PAYMENT đã hết hạn
     */
    List<Booking> findByStatusAndExpiresAtBefore(BookingStatus status, LocalDateTime now);

    /**
     * Khoá pessimistic khi check-in để tránh race condition
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM Booking b WHERE b.id = :id")
    Optional<Booking> findByIdWithLock(@Param("id") Long id);

    /**
     * Staff xem tất cả booking của một slot cụ thể
     */
    List<Booking> findBySlotIdOrderByCreatedAtDesc(Long slotId);

    /**
     * Staff xem booking theo ngày — join qua slot.startTime
     */
    @Query("SELECT b FROM Booking b WHERE b.slot.startTime >= :start AND b.slot.startTime < :end ORDER BY b.createdAt DESC")
    List<Booking> findBySlotStartTimeBetweenOrderByCreatedAtDesc(
            @Param("start") LocalDateTime start,
            @Param("end")   LocalDateTime end
    );

    /**
     * Find booking by bookingCode with pessimistic lock for check-in
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM Booking b WHERE b.bookingCode = :bookingCode")
    Optional<Booking> findByBookingCodeWithLock(@Param("bookingCode") String bookingCode);

    // =========================================================================
    // ANALYTICS — REVENUE STATISTICS
    // =========================================================================

    /**
     * Thống kê doanh thu theo ngày: tổng amount, tổng qty, số booking.
     * Chỉ tính CONFIRMED + CHECKED_IN (đã hoàn tất thanh toán).
     * Trả về Object[]: [0]=date(LocalDate), [1]=sumAmount, [2]=sumQty, [3]=countBookings
     */
    @Query("""
        SELECT CAST(b.createdAt AS LocalDate),
               COALESCE(SUM(b.amount), 0),
               COALESCE(SUM(b.qty), 0),
               COUNT(b.id)
        FROM Booking b
        WHERE b.status IN (:statuses)
          AND b.createdAt >= :from
          AND b.createdAt <  :to
        GROUP BY CAST(b.createdAt AS LocalDate)
        ORDER BY CAST(b.createdAt AS LocalDate)
        """)
    List<Object[]> aggregateRevenueByDay(
            @Param("statuses") List<BookingStatus> statuses,
            @Param("from")     LocalDateTime from,
            @Param("to")       LocalDateTime to
    );

    /**
     * Thống kê doanh thu phân tách theo pool.
     * Trả về Object[]: [0]=poolId, [1]=poolName, [2]=sumAmount, [3]=sumQty, [4]=countBookings
     */
    @Query("""
        SELECT b.pool.id,
               b.pool.name,
               COALESCE(SUM(b.amount), 0),
               COALESCE(SUM(b.qty), 0),
               COUNT(b.id)
        FROM Booking b
        WHERE b.status IN (:statuses)
          AND b.createdAt >= :from
          AND b.createdAt <  :to
        GROUP BY b.pool.id, b.pool.name
        ORDER BY SUM(b.amount) DESC
        """)
    List<Object[]> aggregateRevenueByPool(
            @Param("statuses") List<BookingStatus> statuses,
            @Param("from")     LocalDateTime from,
            @Param("to")       LocalDateTime to
    );
}
