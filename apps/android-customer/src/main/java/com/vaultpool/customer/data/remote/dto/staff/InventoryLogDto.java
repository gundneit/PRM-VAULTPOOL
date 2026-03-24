package com.vaultpool.customer.data.remote.dto.staff;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class InventoryLogDto implements Serializable {

    @SerializedName("id")
    private Long id;

    @SerializedName("slotId")
    private Long slotId;

    @SerializedName("bookingCode")
    private String bookingCode;

    @SerializedName("paymentId")
    private Long paymentId;

    @SerializedName("delta")
    private Integer delta;

    @SerializedName("capacityAfter")
    private Integer capacityAfter;

    @SerializedName("reason")
    private String reason;

    @SerializedName("actor")
    private String actor;

    @SerializedName("createdAt")
    private String createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSlotId() {
        return slotId;
    }

    public void setSlotId(Long slotId) {
        this.slotId = slotId;
    }

    public String getBookingCode() {
        return bookingCode;
    }

    public void setBookingCode(String bookingCode) {
        this.bookingCode = bookingCode;
    }

    public Long getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(Long paymentId) {
        this.paymentId = paymentId;
    }

    public Integer getDelta() {
        return delta;
    }

    public void setDelta(Integer delta) {
        this.delta = delta;
    }

    public Integer getCapacityAfter() {
        return capacityAfter;
    }

    public void setCapacityAfter(Integer capacityAfter) {
        this.capacityAfter = capacityAfter;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getActor() {
        return actor;
    }

    public void setActor(String actor) {
        this.actor = actor;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}