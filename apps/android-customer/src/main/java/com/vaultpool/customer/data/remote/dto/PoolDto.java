package com.vaultpool.customer.data.remote.dto;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class PoolDto {
    @SerializedName("id")
    private Long id;
    
    @SerializedName("name")
    private String name;
    
    @SerializedName("address")
    private String address;
    
    @SerializedName("geoLat")
    private Double geoLat;
    
    @SerializedName("geoLng")
    private Double geoLng;
    
    @SerializedName("description")
    private String description;
    
    @SerializedName("openHours")
    private String openHours;
    
    @SerializedName("status")
    private String status;
    
    @SerializedName("images")
    private List<PoolImageDto> images;

    // Getters and Setters
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getAddress() { return address; }
    public Double getGeoLat() { return geoLat; }
    public Double getGeoLng() { return geoLng; }
    public String getDescription() { return description; }
    public String getOpenHours() { return openHours; }
    public String getStatus() { return status; }
    public List<PoolImageDto> getImages() { return images; }

    public static class PoolImageDto {
        @SerializedName("id")
        private Long id;
        @SerializedName("imageUrl")
        private String imageUrl;
        @SerializedName("sortOrder")
        private Integer sortOrder;

        public String getImageUrl() { return imageUrl; }
    }
}
