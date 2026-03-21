package com.vaultpool.customer.domain.repository;

import com.vaultpool.customer.data.remote.dto.PoolDto;
import com.vaultpool.customer.data.remote.dto.SlotDto;
import com.vaultpool.models.ApiResponse;
import java.util.List;
import io.reactivex.rxjava3.core.Single;

public interface PoolRepository {
    Single<ApiResponse<List<PoolDto>>> getActivePools();
    Single<ApiResponse<PoolDto>> getPoolDetail(Long id);
    Single<ApiResponse<List<SlotDto>>> getSlotsByPoolAndDate(Long id, String date);
}
