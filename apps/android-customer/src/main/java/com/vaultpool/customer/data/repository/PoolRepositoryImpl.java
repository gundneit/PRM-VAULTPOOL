package com.vaultpool.customer.data.repository;

import com.vaultpool.customer.data.remote.api.PoolApi;
import com.vaultpool.customer.data.remote.dto.PoolDto;
import com.vaultpool.customer.data.remote.dto.SlotDto;
import com.vaultpool.customer.domain.repository.PoolRepository;
import com.vaultpool.models.ApiResponse;
import java.util.List;
import io.reactivex.rxjava3.core.Single;

public class PoolRepositoryImpl implements PoolRepository {
    private final PoolApi poolApi;

    public PoolRepositoryImpl(PoolApi poolApi) {
        this.poolApi = poolApi;
    }

    @Override
    public Single<ApiResponse<List<PoolDto>>> getActivePools() {
        return poolApi.getActivePools();
    }

    @Override
    public Single<ApiResponse<PoolDto>> getPoolDetail(Long id) {
        return poolApi.getPoolDetail(id);
    }

    @Override
    public Single<ApiResponse<List<SlotDto>>> getSlotsByPoolAndDate(Long id, String date) {
        return poolApi.getSlotsByPoolAndDate(id, date);
    }
}
