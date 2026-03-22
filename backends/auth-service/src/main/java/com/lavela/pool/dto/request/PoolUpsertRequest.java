package com.lavela.pool.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class PoolUpsertRequest {

    @NotBlank(message = "Pool name is required")
    private String name;

    @NotBlank(message = "Pool address is required")
    private String address;

    private Double geoLat;
    private Double geoLng;
    private String description;
    private String openHours;
    private List<String> imageUrls;
}