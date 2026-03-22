package com.lavela.pool.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "inventory_logs", indexes = {
    @Index(name = "idx_inv_log_slot",    columnList = "slot_id"),
    @Index(name = "idx_inv_log_booking", columnList = "booking_id")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class InventoryLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "slot_id", nullable = false)
    private Slot slot;

    /**
     * Nullable: ghi log khi booking bị null (ví dụ MANUAL_ADJUST)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id")
    private Booking booking;

    /**
     * Optional: reference payment when reason = CONFIRM (audit link).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id")
    private Payment payment;

    /**
     * Âm = giữ chỗ (RESERVE), Dương = trả chỗ (RELEASE/EXPIRE/CANCEL)
     */
    @Column(name = "delta", nullable = false)
    private Integer delta;

    /**
     * Snapshot capacity_available ngay sau khi thay đổi
     */
    @Column(name = "capacity_after", nullable = false)
    private Integer capacityAfter;

    /**
     * RESERVE | RELEASE | CONFIRM | CANCEL | EXPIRE | MANUAL_ADJUST
     */
    @Column(name = "reason", nullable = false, length = 50)
    private String reason;

    /**
     * firebase_uid của user/staff, hoặc "SYSTEM" cho scheduled jobs
     */
    @Column(name = "actor", nullable = false, length = 100)
    private String actor;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
