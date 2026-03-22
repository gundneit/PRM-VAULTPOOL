package com.lavela.pool.integration.zalopay;

import lombok.Builder;
import lombok.Data;

import java.util.Optional;

@Data
@Builder
public class ZaloPayCreateOrderResult {

    private int returnCode;
    private String returnMessage;

    /**
     * Token used for App-to-App payment via ZaloPay SDK.
     */
    private String zpTransToken;

    /**
     * ZaloPay transaction code (optional depending on endpoint).
     */
    private String zpTransId;

    /**
     * Redirect URL (optional).
     */
    private String orderUrl;

    public boolean isSuccess() {
        return returnCode == 1;
    }

    public Optional<String> safeZpTransId() {
        return Optional.ofNullable(zpTransId).filter(s -> !s.isBlank());
    }
}

