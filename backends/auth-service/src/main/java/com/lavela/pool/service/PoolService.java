package com.lavela.pool.service;

import com.lavela.pool.domain.entity.Pool;
import com.lavela.pool.domain.entity.PoolImage;
import com.lavela.pool.domain.entity.Slot;
import com.lavela.pool.dto.request.PoolUpsertRequest;
import com.lavela.pool.dto.request.SlotUpsertRequest;
import com.lavela.pool.dto.response.PoolImageResponse;
import com.lavela.pool.dto.response.PoolResponse;
import com.lavela.pool.dto.response.SlotResponse;
import com.lavela.pool.exception.ResourceNotFoundException;
import com.lavela.pool.repository.PoolRepository;
import com.lavela.pool.repository.SlotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PoolService {

    private static final String ACTIVE_STATUS = "ACTIVE";
    private static final Set<String> POOL_STATUSES = Set.of("ACTIVE", "INACTIVE");
    private static final Set<String> SLOT_STATUSES = Set.of("ACTIVE", "INACTIVE", "FULL");

    private final PoolRepository poolRepository;
    private final SlotRepository slotRepository;

    @Transactional(readOnly = true)
    public List<PoolResponse> getAllPoolsForStaff() {
        return poolRepository.findAllByOrderByNameAsc()
                .stream()
                .map(this::toPoolResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PoolResponse> getPools() {
        return poolRepository.findByStatusOrderByNameAsc(ACTIVE_STATUS)
                .stream()
                .map(this::toPoolResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PoolResponse getPool(Long poolId) {
        Pool pool = poolRepository.findByIdAndStatus(poolId, ACTIVE_STATUS)
                .orElseThrow(() -> new ResourceNotFoundException("Pool not found"));
        return toPoolResponse(pool);
    }

    @Transactional(readOnly = true)
    public List<SlotResponse> getSlots(Long poolId, LocalDate date) {
        if (!poolRepository.existsById(poolId)) {
            throw new ResourceNotFoundException("Pool not found");
        }

        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime nextDay = date.plusDays(1).atStartOfDay();

        return slotRepository.findByPoolIdAndStartTimeGreaterThanEqualAndStartTimeLessThanOrderByStartTimeAsc(
                        poolId,
                        startOfDay,
                        nextDay
                ).stream()
                .map(this::toSlotResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SlotResponse> getAllSlots(Long poolId) {
        if (!poolRepository.existsById(poolId)) {
            throw new ResourceNotFoundException("Pool not found");
        }

        return slotRepository.findByPoolIdOrderByStartTimeAsc(poolId)
                .stream()
                .map(this::toSlotResponse)
                .toList();
    }

    @Transactional
    public PoolResponse createPool(PoolUpsertRequest request) {
        Pool pool = Pool.builder()
                .name(request.getName().trim())
                .address(request.getAddress().trim())
                .geoLat(request.getGeoLat())
                .geoLng(request.getGeoLng())
                .description(request.getDescription())
                .openHours(request.getOpenHours())
                .status(ACTIVE_STATUS)
                .images(new ArrayList<>())
                .build();

        replaceImages(pool, request.getImageUrls());
        return toPoolResponse(poolRepository.save(pool));
    }

    @Transactional
    public PoolResponse updatePool(Long poolId, PoolUpsertRequest request) {
        Pool pool = poolRepository.findById(poolId)
                .orElseThrow(() -> new ResourceNotFoundException("Pool not found"));

        pool.setName(request.getName().trim());
        pool.setAddress(request.getAddress().trim());
        pool.setGeoLat(request.getGeoLat());
        pool.setGeoLng(request.getGeoLng());
        pool.setDescription(request.getDescription());
        pool.setOpenHours(request.getOpenHours());

        if (request.getImageUrls() != null) {
            replaceImages(pool, request.getImageUrls());
        }

        return toPoolResponse(poolRepository.save(pool));
    }

    @Transactional
    public PoolResponse updatePoolStatus(Long poolId, String status) {
        Pool pool = poolRepository.findById(poolId)
                .orElseThrow(() -> new ResourceNotFoundException("Pool not found"));

        pool.setStatus(normalizeStatus(status, POOL_STATUSES, "Invalid pool status"));
        return toPoolResponse(poolRepository.save(pool));
    }

    @Transactional
    public SlotResponse createSlot(Long poolId, SlotUpsertRequest request) {
        Pool pool = poolRepository.findById(poolId)
                .orElseThrow(() -> new ResourceNotFoundException("Pool not found"));

        validateSlotPayload(request);

        Slot slot = Slot.builder()
                .pool(pool)
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .capacityTotal(request.getCapacityTotal())
                .capacityAvailable(request.getCapacityAvailable())
                .price(request.getPrice())
                .status(normalizeStatus(request.getStatus(), SLOT_STATUSES, "Invalid slot status", ACTIVE_STATUS))
                .build();

        return toSlotResponse(slotRepository.save(slot));
    }

    @Transactional
    public SlotResponse updateSlot(Long poolId, Long slotId, SlotUpsertRequest request) {
        Slot slot = slotRepository.findByIdAndPoolId(slotId, poolId)
                .orElseThrow(() -> new ResourceNotFoundException("Slot not found"));

        validateSlotPayload(request);

        slot.setStartTime(request.getStartTime());
        slot.setEndTime(request.getEndTime());
        slot.setCapacityTotal(request.getCapacityTotal());
        slot.setCapacityAvailable(request.getCapacityAvailable());
        slot.setPrice(request.getPrice());
        slot.setStatus(normalizeStatus(request.getStatus(), SLOT_STATUSES, "Invalid slot status", slot.getStatus()));

        return toSlotResponse(slotRepository.save(slot));
    }

    @Transactional
    public SlotResponse updateSlotStatus(Long poolId, Long slotId, String status) {
        Slot slot = slotRepository.findByIdAndPoolId(slotId, poolId)
                .orElseThrow(() -> new ResourceNotFoundException("Slot not found"));

        slot.setStatus(normalizeStatus(status, SLOT_STATUSES, "Invalid slot status"));
        return toSlotResponse(slotRepository.save(slot));
    }

    private PoolResponse toPoolResponse(Pool pool) {
        return PoolResponse.builder()
                .id(pool.getId())
                .name(pool.getName())
                .address(pool.getAddress())
                .geoLat(pool.getGeoLat())
                .geoLng(pool.getGeoLng())
                .description(pool.getDescription())
                .openHours(pool.getOpenHours())
                .status(pool.getStatus())
                .images(pool.getImages().stream().map(this::toPoolImageResponse).toList())
                .build();
    }

    private PoolImageResponse toPoolImageResponse(PoolImage image) {
        return PoolImageResponse.builder()
                .id(image.getId())
                .imageUrl(image.getImageUrl())
                .sortOrder(image.getSortOrder())
                .build();
    }

    private SlotResponse toSlotResponse(Slot slot) {
        return SlotResponse.builder()
                .id(slot.getId())
                .poolId(slot.getPool().getId())
                .startTime(slot.getStartTime())
                .endTime(slot.getEndTime())
                .capacityTotal(slot.getCapacityTotal())
                .capacityAvailable(slot.getCapacityAvailable())
                .price(slot.getPrice())
                .status(slot.getStatus())
                .build();
    }

    private void replaceImages(Pool pool, List<String> imageUrls) {
        pool.getImages().clear();

        if (imageUrls == null) {
            return;
        }

        int order = 1;
        for (String imageUrl : imageUrls) {
            if (!StringUtils.hasText(imageUrl)) {
                continue;
            }
            PoolImage image = PoolImage.builder()
                    .pool(pool)
                    .imageUrl(imageUrl.trim())
                    .sortOrder(order++)
                    .build();
            pool.getImages().add(image);
        }
    }

    private void validateSlotPayload(SlotUpsertRequest request) {
        if (!request.getEndTime().isAfter(request.getStartTime())) {
            throw new IllegalArgumentException("endTime must be after startTime");
        }
        if (request.getCapacityAvailable() > request.getCapacityTotal()) {
            throw new IllegalArgumentException("capacityAvailable must be <= capacityTotal");
        }
    }

    private String normalizeStatus(String status, Set<String> allowed, String errorMessage) {
        if (!StringUtils.hasText(status)) {
            throw new IllegalArgumentException(errorMessage);
        }
        String normalized = status.trim().toUpperCase();
        if (!allowed.contains(normalized)) {
            throw new IllegalArgumentException(errorMessage);
        }
        return normalized;
    }

    private String normalizeStatus(String status, Set<String> allowed, String errorMessage, String defaultValue) {
        if (!StringUtils.hasText(status)) {
            return defaultValue;
        }
        return normalizeStatus(status, allowed, errorMessage);
    }
}