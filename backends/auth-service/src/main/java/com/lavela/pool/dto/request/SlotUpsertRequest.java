package com.lavela.pool.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class SlotUpsertRequest {

    @NotNull(message = "startTime is required")
    private LocalDateTime startTime;

    @NotNull(message = "endTime is required")
    private LocalDateTime endTime;

    @NotNull(message = "capacityTotal is required")
    @Positive(message = "capacityTotal must be > 0")
    private Integer capacityTotal;

    @NotNull(message = "capacityAvailable is required")
    @Positive(message = "capacityAvailable must be > 0")
    private Integer capacityAvailable;

    @NotNull(message = "price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "price must be > 0")
    private BigDecimal price;

    private String status;
}