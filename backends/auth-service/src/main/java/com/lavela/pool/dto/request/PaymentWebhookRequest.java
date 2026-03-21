package com.lavela.pool.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * Legacy generic webhook payload from payment provider.
 * (No longer used by the ZaloPay-only implementation. Kept for backward compatibility.)
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentWebhookRequest {

    /**
     * Provider transaction ID (idempotency key).
     */
    private String providerTxnId;

    /**
     * SUCCESS | FAILED | CANCELED
     */
    private String status;

    /**
     * Optional: provider signature for verification.
     */
    private String signature;
}
