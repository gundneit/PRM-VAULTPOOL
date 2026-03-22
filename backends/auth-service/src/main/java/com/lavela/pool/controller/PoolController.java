package com.lavela.pool.controller;

import com.lavela.pool.dto.response.ApiResponse;
import com.lavela.pool.dto.response.PoolResponse;
import com.lavela.pool.dto.response.SlotResponse;
import com.lavela.pool.service.PoolService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

@Tag(name = "Pools", description = "Pool and slot catalog")
@RestController
@RequestMapping("/pools")
@RequiredArgsConstructor
public class PoolController {

    private final PoolService poolService;

    @Operation(summary = "Get active pools")
    @GetMapping
    public ApiResponse<List<PoolResponse>> getPools() {
        return ApiResponse.ok(poolService.getPools());
    }

    @Operation(summary = "Get pool detail")
    @GetMapping("/{id}")
    public ApiResponse<PoolResponse> getPool(@PathVariable Long id) {
        return ApiResponse.ok(poolService.getPool(id));
    }

    @Operation(summary = "Get slots by pool and date")
    @GetMapping("/{id}/slots")
    public ApiResponse<List<SlotResponse>> getSlots(
            @PathVariable Long id,
            @RequestParam String date
    ) {
        try {
            LocalDate parsedDate = LocalDate.parse(date.trim());
            return ApiResponse.ok(poolService.getSlots(id, parsedDate));
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Invalid date format. Expected YYYY-MM-DD");
        }
    }

    @Operation(summary = "Get all slots by pool id")
    @GetMapping("/{id}/slots/all")
    public ApiResponse<List<SlotResponse>> getAllSlots(@PathVariable Long id) {
        return ApiResponse.ok(poolService.getAllSlots(id));
    }
}