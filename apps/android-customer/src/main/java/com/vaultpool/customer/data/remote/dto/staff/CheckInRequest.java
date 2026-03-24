package com.vaultpool.customer.data.remote.dto.staff;

import com.google.gson.annotations.SerializedName;

public class CheckInRequest {

    @SerializedName("bookingCode")
    private String bookingCode;

    public CheckInRequest(String bookingCode) {
        this.bookingCode = bookingCode;
    }

    public String getBookingCode() {
        return bookingCode;
    }

    public void setBookingCode(String bookingCode) {
        this.bookingCode = bookingCode;
    }
}
