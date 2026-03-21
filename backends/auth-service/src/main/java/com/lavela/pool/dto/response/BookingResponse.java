package com.lavela.pool.dto.response;

import com.lavela.pool.domain.enums.BookingStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class BookingResponse {

    private Long id;
    private String bookingCode;

    private Long slotId;
    private Long poolId;
    private String poolName;
    private String poolAddress;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private Integer qty;
    private BigDecimal amount;

    private BookingStatus status;
    private String paymentStatus;

    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
}
