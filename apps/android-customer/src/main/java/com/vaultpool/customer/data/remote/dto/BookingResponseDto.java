package com.vaultpool.customer.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class BookingResponseDto {

    @SerializedName("id")
    private Long id;

    @SerializedName("bookingCode")
    private String bookingCode;

    public Long getId() {
        return id;
    }

    public String getBookingCode() {
        return bookingCode;
    }
}

