package com.lavela.pool.controller;

import com.lavela.pool.dto.request.PoolUpsertRequest;
import com.lavela.pool.dto.request.SlotUpsertRequest;
import com.lavela.pool.dto.request.StatusUpdateRequest;
import com.lavela.pool.dto.response.ApiResponse;
import com.lavela.pool.dto.response.PoolResponse;
import com.lavela.pool.dto.response.SlotResponse;
import com.lavela.pool.service.PoolService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "Staff Pools", description = "Pool and slot management for STAFF/ADMIN")
@RestController
@RequestMapping("/api/staff/pools")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('STAFF','ADMIN')")
@SecurityRequirement(name = "bearerAuth")
public class StaffPoolController {

    private final PoolService poolService;

    @Operation(summary = "List all pools for management")
    @GetMapping
    public ApiResponse<List<PoolResponse>> getAllPools() {
        return ApiResponse.ok(poolService.getAllPoolsForStaff());
    }

    @Operation(summary = "Create pool")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<PoolResponse> createPool(@Valid @ModelAttribute PoolUpsertRequest request) {
        return ApiResponse.ok(poolService.createPool(request));
    }

    @Operation(summary = "Update pool")
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<PoolResponse> updatePool(
            @PathVariable Long id,
            @Valid @ModelAttribute PoolUpsertRequest request
    ) {
        return ApiResponse.ok(poolService.updatePool(id, request));
    }

    @Operation(summary = "Update pool status")
    @PatchMapping("/{id}/status")
    public ApiResponse<PoolResponse> updatePoolStatus(
            @PathVariable Long id,
            @Valid @RequestBody StatusUpdateRequest request
    ) {
        return ApiResponse.ok(poolService.updatePoolStatus(id, request.getStatus()));
    }

    @Operation(summary = "Create slot under pool")
    @PostMapping("/{id}/slots")
    public ApiResponse<SlotResponse> createSlot(
            @PathVariable Long id,
            @Valid @RequestBody SlotUpsertRequest request
    ) {
        return ApiResponse.ok(poolService.createSlot(id, request));
    }

    @Operation(summary = "Update slot")
    @PutMapping("/{id}/slots/{slotId}")
    public ApiResponse<SlotResponse> updateSlot(
            @PathVariable Long id,
            @PathVariable Long slotId,
            @Valid @RequestBody SlotUpsertRequest request
    ) {
        return ApiResponse.ok(poolService.updateSlot(id, slotId, request));
    }

    @Operation(summary = "Update slot status")
    @PatchMapping("/{id}/slots/{slotId}/status")
    public ApiResponse<SlotResponse> updateSlotStatus(
            @PathVariable Long id,
            @PathVariable Long slotId,
            @Valid @RequestBody StatusUpdateRequest request
    ) {
        return ApiResponse.ok(poolService.updateSlotStatus(id, slotId, request.getStatus()));
    }
}