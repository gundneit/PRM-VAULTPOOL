package com.lavela.pool.integration.zalopay;

import lombok.Builder;
import lombok.Data;

/**
 * Minimal response mapping for ZaloPay order-query (/v2/query).
 */
@Data
@Builder
public class ZaloPayQueryOrderResult {
    private int returnCode;
    private String returnMessage;
    private Integer subReturnCode;
    private String subReturnMessage;
    /** From ZaloPay JSON field {@code is_processing} (distinct from {@link #isProcessing()}). */
    private boolean zpProcessingFlag;

    /**
     * Per ZaloPay Integration Doc §3 Query Order Status:
     * {@code return_code}: 1 = SUCCESS, 2 = FAIL, 3 = PROCESSING.
     */
    public boolean isSuccess() {
        return returnCode == 1;
    }

    public boolean isProcessing() {
        return zpProcessingFlag || returnCode == 3;
    }

    public boolean isFailure() {
        return !isSuccess() && !isProcessing();
    }
}

