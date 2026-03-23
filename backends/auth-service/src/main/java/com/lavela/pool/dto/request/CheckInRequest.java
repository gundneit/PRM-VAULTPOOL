package com.lavela.pool.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CheckInRequest {

    @NotBlank(message = "bookingCode is required")
    private String bookingCode;
}
