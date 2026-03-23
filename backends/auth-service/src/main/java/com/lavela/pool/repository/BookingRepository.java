package com.lavela.pool.repository;

import com.lavela.pool.domain.entity.Booking;
import com.lavela.pool.domain.enums.BookingStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
}
