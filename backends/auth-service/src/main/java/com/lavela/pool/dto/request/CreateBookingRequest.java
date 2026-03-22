package com.lavela.pool.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import com.lavela.pool.domain.enums.BookingStatus;

@Data
public class CreateBookingRequest {

    @NotNull(message = "slotId is required")
    private Long slotId;

    @NotNull(message = "qty is required")
    @Min(value = 1, message = "qty must be at least 1")
    @Max(value = 10, message = "qty must not exceed 10")
    private Integer qty;

    // FE truyền lên IN_CART hoặc PENDING_PAYMENT
    private BookingStatus status;
}
