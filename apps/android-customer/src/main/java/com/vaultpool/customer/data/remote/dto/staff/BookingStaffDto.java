package com.vaultpool.customer.data.remote.dto.staff;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class BookingStaffDto implements Serializable {
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

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getBookingCode() { return bookingCode; }
    public void setBookingCode(String bookingCode) { this.bookingCode = bookingCode; }
    public Long getSlotId() { return slotId; }
    public void setSlotId(Long slotId) { this.slotId = slotId; }
    public Long getPoolId() { return poolId; }
    public void setPoolId(Long poolId) { this.poolId = poolId; }
    public String getPoolName() { return poolName; }
    public void setPoolName(String poolName) { this.poolName = poolName; }
    public String getPoolAddress() { return poolAddress; }
    public void setPoolAddress(String poolAddress) { this.poolAddress = poolAddress; }
    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }
    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }
    public Integer getQty() { return qty; }
    public void setQty(Integer qty) { this.qty = qty; }
    public Long getAmount() { return amount; }
    public void setAmount(Long amount) { this.amount = amount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
    public String getExpiresAt() { return expiresAt; }
    public void setExpiresAt(String expiresAt) { this.expiresAt = expiresAt; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
