package com.lavela.pool.domain.enums;

/**
 * Payment state machine (SRS):
 * CREATED -> PENDING -> SUCCESS
 * Failure branches: FAILED, REFUNDED
 * (CANCELED treated as FAILED for booking release.)
 */
public enum PaymentStatus {
    CREATED,
    PENDING,
    SUCCESS,
    FAILED,
    REFUNDED
}
