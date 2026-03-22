package com.vaultpool.customer.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class CreatePaymentRequestDto {

    @SerializedName("bookingId")
    private Long bookingId;

    @SerializedName("method")
    private String method;

    public CreatePaymentRequestDto(Long bookingId, String method) {
        this.bookingId = bookingId;
        this.method = method;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public String getMethod() {
        return method;
    }
}

