package com.lavela.pool.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class SlotResponse {
	private Long id;
	private Long poolId;
	private LocalDateTime startTime;
	private LocalDateTime endTime;
	private Integer capacityTotal;
	private Integer capacityAvailable;
	private BigDecimal price;
	private String status;
}
