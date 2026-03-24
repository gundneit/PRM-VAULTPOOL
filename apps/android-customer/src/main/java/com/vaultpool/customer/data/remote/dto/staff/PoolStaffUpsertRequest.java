package com.vaultpool.customer.data.remote.dto.staff;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PoolStaffUpsertRequest {
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

    @SerializedName("imageUrls")
    private List<String> imageUrls;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Double getGeoLat() {
        return geoLat;
    }

    public void setGeoLat(Double geoLat) {
        this.geoLat = geoLat;
    }

    public Double getGeoLng() {
        return geoLng;
    }

    public void setGeoLng(Double geoLng) {
        this.geoLng = geoLng;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getOpenHours() {
        return openHours;
    }

    public void setOpenHours(String openHours) {
        this.openHours = openHours;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }
}
