package com.lavela.pool.repository;

import com.lavela.pool.domain.entity.Slot;
import org.springframework.data.jpa.repository.JpaRepository;
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
}