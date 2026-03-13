package com.lavela.pool.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PoolResponse {
    private Long id;
    private String name;
    private String address;
    private Double geoLat;
    private Double geoLng;
    private String description;
    private String openHours;
    private String status;
    private List<PoolImageResponse> images;
}