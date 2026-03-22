package com.vaultpool.customer.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class SlotDto {
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
    private Double price;
    
    @SerializedName("status")
    private String status;

    // Getters
    public Long getId() { return id; }
    public Long getPoolId() { return poolId; }
    public String getStartTime() { return startTime; }
    public String getEndTime() { return endTime; }
    public Integer getCapacityTotal() { return capacityTotal; }
    public Integer getCapacityAvailable() { return capacityAvailable; }
    public Double getPrice() { return price; }
    public String getStatus() { return status; }
}
