package com.vaultpool.customer.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class BookingResponseDto {

    @SerializedName("id")
    private Long id;

    @SerializedName("bookingCode")
    private String bookingCode;

    @SerializedName("slotId")
    private Long slotId;

    @SerializedName("poolId")
    private Long poolId;

    @SerializedName("poolName")
    private String poolName;

    @SerializedName("poolAddress")
    private String poolAddress;

    @SerializedName("startTime")
    private String startTime;

    @SerializedName("endTime")
    private String endTime;

    @SerializedName("qty")
    private Integer qty;

    @SerializedName("amount")
    private Long amount;

    @SerializedName("status")
    private String status;

    @SerializedName("paymentStatus")
    private String paymentStatus;

    @SerializedName("expiresAt")
    private String expiresAt;

    @SerializedName("createdAt")
    private String createdAt;

    public Long getId() {
        return id;
    }

    public String getBookingCode() {
        return bookingCode;
    }

    public Long getSlotId() {
        return slotId;
    }

    public Long getPoolId() {
        return poolId;
    }

    public String getPoolName() {
        return poolName;
    }

    public String getPoolAddress() {
        return poolAddress;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public Integer getQty() {
        return qty;
    }

    public Long getAmount() {
        return amount;
    }

    public String getStatus() {
        return status;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public String getExpiresAt() {
        return expiresAt;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}

