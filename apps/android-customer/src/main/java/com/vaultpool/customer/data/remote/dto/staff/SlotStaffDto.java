package com.vaultpool.customer.data.remote.dto.staff;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class SlotStaffDto implements Serializable {
    @SerializedName("id")
    private Long id;
    @SerializedName("poolId")
    private Long poolId;
    @SerializedName("startTime")
    private String startTime;
    @SerializedName("endTime")
    private String endTime;
    @SerializedName("capacityTotal")
    private Integer capacityTotal;
    @SerializedName("capacityAvailable")
    private Integer capacityAvailable;
    @SerializedName("price")
    private Long price;
    @SerializedName("status")
    private String status;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getPoolId() { return poolId; }
    public void setPoolId(Long poolId) { this.poolId = poolId; }
    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }
    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }
    public Integer getCapacityTotal() { return capacityTotal; }
    public void setCapacityTotal(Integer capacityTotal) { this.capacityTotal = capacityTotal; }
    public Integer getCapacityAvailable() { return capacityAvailable; }
    public void setCapacityAvailable(Integer capacityAvailable) { this.capacityAvailable = capacityAvailable; }
    public Long getPrice() { return price; }
    public void setPrice(Long price) { this.price = price; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
