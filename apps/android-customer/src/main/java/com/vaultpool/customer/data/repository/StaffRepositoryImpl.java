package com.vaultpool.customer.data.repository;

import com.vaultpool.customer.data.remote.api.StaffApi;
import com.vaultpool.customer.data.remote.dto.ApiResponse;
import com.vaultpool.customer.data.remote.dto.staff.BookingStaffDto;
import com.vaultpool.customer.data.remote.dto.staff.PoolStaffDto;
import com.vaultpool.customer.data.remote.dto.staff.SlotStaffDto;
import com.vaultpool.customer.domain.model.Result;
import com.vaultpool.customer.domain.repository.StaffRepository;

import java.util.HashMap;
import java.util.List;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class StaffRepositoryImpl implements StaffRepository {

    private final StaffApi staffApi;

    public StaffRepositoryImpl(StaffApi staffApi) {
        this.staffApi = staffApi;
    }

    private String formatToken(String token) {
        if (token == null) return "";
        return token.startsWith("Bearer ") ? token : "Bearer " + token;
    }

    @Override
    public Single<Result<List<BookingStaffDto>>> getStaffBookings(String token) {
        return staffApi.getStaffBookings(formatToken(token))
                .map(this::handleResponse)
                .subscribeOn(Schedulers.io());
    }

    @Override
    public Single<Result<List<PoolStaffDto>>> getStaffPools(String token) {
        return staffApi.getStaffPools(formatToken(token))
                .map(this::handleResponse)
                .subscribeOn(Schedulers.io());
    }

    @Override
    public Single<Result<List<SlotStaffDto>>> getPoolSlots(String token, Long poolId) {
        return staffApi.getPoolSlots(poolId)
                .map(this::handleResponse)
                .subscribeOn(Schedulers.io());
    }

    @Override
    public Single<Result<List<SlotStaffDto>>> getSlotsByDate(Long poolId, String date) {
        return staffApi.getSlotsByDate(poolId, date)
                .map(this::handleResponse)
                .subscribeOn(Schedulers.io());
    }

    @Override
    public Single<Result<PoolStaffDto>> createPool(String token, PoolStaffDto pool) {
        return staffApi.createPool(formatToken(token), pool)
                .map(this::handleResponse)
                .subscribeOn(Schedulers.io());
    }

    @Override
    public Single<Result<PoolStaffDto>> updatePool(String token, Long id, PoolStaffDto pool) {
        return staffApi.updatePool(formatToken(token), id, pool)
                .map(this::handleResponse)
                .subscribeOn(Schedulers.io());
    }

    @Override
    public Single<Result<PoolStaffDto>> updatePoolStatus(String token, Long id, String status) {
        java.util.Map<String, String> body = new java.util.HashMap<>();
        body.put("status", status);
        return staffApi.updatePoolStatus(formatToken(token), id, body)
                .map(this::handleResponse)
                .subscribeOn(Schedulers.io());
    }

    @Override
    public Single<Result<SlotStaffDto>> createSlot(String token, Long poolId, SlotStaffDto slot) {
        return staffApi.createSlot(formatToken(token), poolId, slot)
                .map(this::handleResponse)
                .subscribeOn(Schedulers.io());
    }

    @Override
    public Single<Result<SlotStaffDto>> updateSlot(String token, Long poolId, Long slotId, SlotStaffDto slot) {
        return staffApi.updateSlot(formatToken(token), poolId, slotId, slot)
                .map(this::handleResponse)
                .subscribeOn(Schedulers.io());
    }

    @Override
    public Single<Result<SlotStaffDto>> updateSlotStatus(String token, Long poolId, Long slotId, String status) {
        java.util.Map<String, String> body = new java.util.HashMap<>();
        body.put("status", status);
        return staffApi.updateSlotStatus(formatToken(token), poolId, slotId, body)
                .map(this::handleResponse)
                .subscribeOn(Schedulers.io());
    }

    private <T> Result<T> handleResponse(ApiResponse<T> response) {
        if (response.isSuccess()) {
            return Result.success(response.getData());
        } else {
            return Result.failure(response.getMessage());
        }
    }
}
