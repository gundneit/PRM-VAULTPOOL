package com.lavela.pool.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreatePaymentRequest {

    @NotNull(message = "bookingId is required")
    private Long bookingId;

    /**
     * Payment method/provider: currently supports `ZALOPAY` only.
     */
    @NotBlank(message = "method is required")
    private String method;
}
