package com.vaultpool.customer.data.repository;

import com.vaultpool.customer.data.remote.api.StaffApi;
import com.vaultpool.customer.data.remote.dto.ApiResponse;
import com.vaultpool.customer.data.remote.dto.staff.BookingStaffDto;
import com.vaultpool.customer.data.remote.dto.staff.CheckInRequest;
import com.vaultpool.customer.data.remote.dto.staff.InventoryLogDto;
import com.vaultpool.customer.data.remote.dto.staff.PoolStaffDto;
import com.vaultpool.customer.data.remote.dto.staff.RevenueAnalyticsDto;
import com.vaultpool.customer.data.remote.dto.staff.SlotStaffDto;
import com.vaultpool.customer.domain.model.Result;
import com.vaultpool.customer.domain.repository.StaffRepository;

import java.io.File;
import java.util.List;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

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
    public Single<Result<BookingStaffDto>> checkInByQr(String token, String bookingCode) {
        return staffApi.checkInByQr(formatToken(token), new CheckInRequest(bookingCode))
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
    public Single<Result<PoolStaffDto>> createPool(String token, PoolStaffDto pool, File imageFile) {
        return staffApi.createPool(
                        formatToken(token),
                        requiredTextPart(pool.getName()),
                        requiredTextPart(pool.getAddress()),
                        nullableTextPart(toStringValue(pool.getGeoLat())),
                        nullableTextPart(toStringValue(pool.getGeoLng())),
                        nullableTextPart(pool.getDescription()),
                        nullableTextPart(pool.getOpenHours()),
                        createImagePart(imageFile)
                )
                .map(this::handleResponse)
                .subscribeOn(Schedulers.io());
    }

    @Override
    public Single<Result<PoolStaffDto>> updatePool(String token, Long id, PoolStaffDto pool, File imageFile) {
        return staffApi.updatePool(
                        formatToken(token),
                        id,
                        requiredTextPart(pool.getName()),
                        requiredTextPart(pool.getAddress()),
                        nullableTextPart(toStringValue(pool.getGeoLat())),
                        nullableTextPart(toStringValue(pool.getGeoLng())),
                        nullableTextPart(pool.getDescription()),
                        nullableTextPart(pool.getOpenHours()),
                        createImagePart(imageFile)
                )
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

    @Override
    public Single<Result<List<RevenueAnalyticsDto>>> getRevenueByTime(String token, String from, String to, String granularity) {
        return staffApi.getRevenueByTime(formatToken(token), from, to, granularity)
                .map(this::handleResponse)
                .subscribeOn(Schedulers.io());
    }

    @Override
    public Single<Result<List<RevenueAnalyticsDto>>> getRevenueByPool(String token, String from, String to) {
        return staffApi.getRevenueByPool(formatToken(token), from, to)
                .map(this::handleResponse)
                .subscribeOn(Schedulers.io());
    }

    @Override
    public Single<Result<List<InventoryLogDto>>> getInventoryLogs(String token, String from, String to) {
        return staffApi.getInventoryLogs(formatToken(token), from, to)
                .map(this::handleResponse)
                .subscribeOn(Schedulers.io());
    }

    @Override
    public Single<Result<List<InventoryLogDto>>> getInventoryLogsBySlot(String token, Long slotId) {
        return staffApi.getInventoryLogsBySlot(formatToken(token), slotId)
                .map(this::handleResponse)
                .subscribeOn(Schedulers.io());
    }

    @Override
    public Single<Result<List<InventoryLogDto>>> getInventoryLogsByBooking(String token, String bookingCode) {
        return staffApi.getInventoryLogsByBooking(formatToken(token), bookingCode)
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

    private RequestBody requiredTextPart(String value) {
        return RequestBody.create(value == null ? "" : value, MultipartBody.FORM);
    }

    private RequestBody nullableTextPart(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return RequestBody.create(value.trim(), MultipartBody.FORM);
    }

    private String toStringValue(Double value) {
        return value == null ? null : String.valueOf(value);
    }

    private MultipartBody.Part createImagePart(File imageFile) {
        if (imageFile == null || !imageFile.exists()) {
            return null;
        }

        RequestBody requestFile = RequestBody.create(imageFile, MediaType.parse("image/*"));
        return MultipartBody.Part.createFormData("image", imageFile.getName(), requestFile);
    }
}
