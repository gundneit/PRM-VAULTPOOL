package com.vaultpool.customer.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class PaymentResponseDto {

    @SerializedName("id")
    private Long id;

    @SerializedName("bookingId")
    private Long bookingId;

    @SerializedName("providerTxnId")
    private String providerTxnId;

    @SerializedName("redirectUrl")
    private String redirectUrl;

    public Long getId() {
        return id;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public String getProviderTxnId() {
        return providerTxnId;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }
}

