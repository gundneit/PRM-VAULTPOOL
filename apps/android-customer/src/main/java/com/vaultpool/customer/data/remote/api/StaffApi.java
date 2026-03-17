package com.vaultpool.customer.data.remote.api;

import com.vaultpool.customer.data.remote.dto.ApiResponse;
import com.vaultpool.customer.data.remote.dto.staff.BookingStaffDto;
import com.vaultpool.customer.data.remote.dto.staff.PoolStaffDto;
import com.vaultpool.customer.data.remote.dto.staff.SlotStaffDto;

import java.util.List;
import io.reactivex.rxjava3.core.Single;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface StaffApi {
    @GET("api/staff/bookings")
    Single<ApiResponse<List<BookingStaffDto>>> getStaffBookings(@Header("Authorization") String token);

    @GET("api/staff/pools")
    Single<ApiResponse<List<PoolStaffDto>>> getStaffPools(@Header("Authorization") String token);

    @POST("api/staff/pools")
    Single<ApiResponse<PoolStaffDto>> createPool(@Header("Authorization") String token, @Body PoolStaffDto pool);

    @PUT("api/staff/pools/{id}")
    Single<ApiResponse<PoolStaffDto>> updatePool(@Header("Authorization") String token, @Path("id") Long id, @Body PoolStaffDto pool);

    @PATCH("api/staff/pools/{id}/status")
    Single<ApiResponse<PoolStaffDto>> updatePoolStatus(@Header("Authorization") String token, @Path("id") Long id);

    @POST("api/staff/pools/{poolId}/slots")
    Single<ApiResponse<SlotStaffDto>> createSlot(@Header("Authorization") String token, @Path("poolId") Long poolId, @Body SlotStaffDto slot);

    @PUT("api/staff/pools/{poolId}/slots/{slotId}")
    Single<ApiResponse<SlotStaffDto>> updateSlot(@Header("Authorization") String token, @Path("poolId") Long poolId, @Path("slotId") Long slotId, @Body SlotStaffDto slot);

    @PATCH("api/staff/pools/{poolId}/slots/{slotId}/status")
    Single<ApiResponse<SlotStaffDto>> updateSlotStatus(@Header("Authorization") String token, @Path("poolId") Long poolId, @Path("slotId") Long slotId);
}
