package com.lavela.pool.dto.response;

import com.lavela.pool.domain.enums.PaymentStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class PaymentResponse {

    private Long id;
    private Long bookingId;
    private String provider;
    private String providerTxnId;
    private BigDecimal amount;
    private String currency;
    private PaymentStatus status;
    /**
     * URL to redirect user to payment gateway (or in-app SDK params).
     */
    private String redirectUrl;
    private LocalDateTime paidAt;
    private LocalDateTime refundedAt;
    private LocalDateTime createdAt;
}
