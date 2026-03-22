package com.lavela.pool.repository;

import com.lavela.pool.domain.entity.Slot;
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
public interface SlotRepository extends JpaRepository<Slot, Long> {
    List<Slot> findByPoolIdOrderByStartTimeAsc(Long poolId);

    List<Slot> findByPoolIdAndStartTimeGreaterThanEqualAndStartTimeLessThanOrderByStartTimeAsc(
            Long poolId,
            LocalDateTime startInclusive,
            LocalDateTime endExclusive
    );

    Optional<Slot> findByIdAndPoolId(Long id, Long poolId);

    /**
     * Pessimistic write lock — dùng khi tạo booking để tránh overbooking concurrent
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Slot s WHERE s.id = :id")
    Optional<Slot> findByIdForUpdate(@Param("id") Long id);
}