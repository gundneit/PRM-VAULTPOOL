package com.vaultpool.customer.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class CreateBookingRequestDto {

    @SerializedName("slotId")
    private Long slotId;

    @SerializedName("qty")
    private Integer qty;

    @SerializedName("status")
    private String status;

    public CreateBookingRequestDto(Long slotId, Integer qty) {
        this(slotId, qty, "IN_CART");
    }

    public CreateBookingRequestDto(Long slotId, Integer qty, String status) {
        this.slotId = slotId;
        this.qty = qty;
        this.status = status;
    }

    public Long getSlotId() {
        return slotId;
    }

    public Integer getQty() {
        return qty;
    }

    public String getStatus() {
        return status;
    }
}

