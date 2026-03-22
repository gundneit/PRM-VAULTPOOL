package com.vaultpool.customer.domain.repository;

import com.vaultpool.customer.data.remote.dto.staff.BookingStaffDto;
import com.vaultpool.customer.data.remote.dto.staff.PoolStaffDto;
import com.vaultpool.customer.data.remote.dto.staff.SlotStaffDto;
import com.vaultpool.customer.domain.model.Result;

import java.util.List;
import io.reactivex.rxjava3.core.Single;

public interface StaffRepository {
    Single<Result<List<BookingStaffDto>>> getStaffBookings(String token);
    Single<Result<List<PoolStaffDto>>> getStaffPools(String token);
    Single<Result<List<SlotStaffDto>>> getPoolSlots(String token, Long poolId);
    Single<Result<List<SlotStaffDto>>> getSlotsByDate(Long poolId, String date);
    Single<Result<PoolStaffDto>> createPool(String token, PoolStaffDto pool);
    Single<Result<PoolStaffDto>> updatePool(String token, Long id, PoolStaffDto pool);
    Single<Result<PoolStaffDto>> updatePoolStatus(String token, Long id, String status);
    Single<Result<SlotStaffDto>> createSlot(String token, Long poolId, SlotStaffDto slot);
    Single<Result<SlotStaffDto>> updateSlot(String token, Long poolId, Long slotId, SlotStaffDto slot);
    Single<Result<SlotStaffDto>> updateSlotStatus(String token, Long poolId, Long slotId, String status);
}
