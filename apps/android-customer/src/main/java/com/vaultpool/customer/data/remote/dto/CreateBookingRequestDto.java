package com.vaultpool.customer.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class CreateBookingRequestDto {

    @SerializedName("slotId")
    private Long slotId;

    @SerializedName("qty")
    private Integer qty;

    public CreateBookingRequestDto(Long slotId, Integer qty) {
        this.slotId = slotId;
        this.qty = qty;
    }

    public Long getSlotId() {
        return slotId;
    }

    public Integer getQty() {
        return qty;
    }
}

