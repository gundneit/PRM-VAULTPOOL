package com.vaultpool.customer.data.remote.dto.staff;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

public class PoolStaffDto implements Serializable {
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
    private List<ImageStaffDto> images;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public Double getGeoLat() { return geoLat; }
    public void setGeoLat(Double geoLat) { this.geoLat = geoLat; }
    public Double getGeoLng() { return geoLng; }
    public void setGeoLng(Double geoLng) { this.geoLng = geoLng; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getOpenHours() { return openHours; }
    public void setOpenHours(String openHours) { this.openHours = openHours; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public List<ImageStaffDto> getImages() { return images; }
    public void setImages(List<ImageStaffDto> images) { this.images = images; }
}
