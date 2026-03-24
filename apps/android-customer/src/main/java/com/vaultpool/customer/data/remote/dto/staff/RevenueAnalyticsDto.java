package com.vaultpool.customer.data.remote.dto.staff;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class RevenueAnalyticsDto implements Serializable {

    @SerializedName("period")
    private String period;

    @SerializedName("totalRevenue")
    private Long totalRevenue;

    @SerializedName("ticketsSold")
    private Integer ticketsSold;

    @SerializedName("bookingCount")
    private Integer bookingCount;

    @SerializedName("poolId")
    private Long poolId;

    @SerializedName("poolName")
    private String poolName;

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public Long getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(Long totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public Integer getTicketsSold() {
        return ticketsSold;
    }

    public void setTicketsSold(Integer ticketsSold) {
        this.ticketsSold = ticketsSold;
    }

    public Integer getBookingCount() {
        return bookingCount;
    }

    public void setBookingCount(Integer bookingCount) {
        this.bookingCount = bookingCount;
    }

    public Long getPoolId() {
        return poolId;
    }

    public void setPoolId(Long poolId) {
        this.poolId = poolId;
    }

    public String getPoolName() {
        return poolName;
    }

    public void setPoolName(String poolName) {
        this.poolName = poolName;
    }
}