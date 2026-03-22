package com.lavela.pool.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PoolImageResponse {
    private Long id;
    private String imageUrl;
    private Integer sortOrder;
}