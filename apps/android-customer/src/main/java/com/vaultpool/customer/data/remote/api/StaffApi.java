package com.vaultpool.customer.data.remote.api;

import com.vaultpool.customer.data.remote.dto.ApiResponse;
import com.vaultpool.customer.data.remote.dto.staff.BookingStaffDto;
import com.vaultpool.customer.data.remote.dto.staff.CheckInRequest;
import com.vaultpool.customer.data.remote.dto.staff.InventoryLogDto;
import com.vaultpool.customer.data.remote.dto.staff.PoolStaffDto;
import com.vaultpool.customer.data.remote.dto.staff.RevenueAnalyticsDto;
import com.vaultpool.customer.data.remote.dto.staff.SlotStaffDto;

import java.util.List;
import io.reactivex.rxjava3.core.Single;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.PATCH;
import retrofit2.http.Part;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface StaffApi {
    // Staff Operations (Private)
    @GET("api/staff/bookings")
    Single<ApiResponse<List<BookingStaffDto>>> getStaffBookings(@Header("Authorization") String token);

    @GET("api/staff/pools")
    Single<ApiResponse<List<PoolStaffDto>>> getStaffPools(@Header("Authorization") String token);

    @POST("bookings/checkin")
    Single<ApiResponse<BookingStaffDto>> checkInByQr(
            @Header("Authorization") String token,
            @Body CheckInRequest request
    );

    @Multipart
    @POST("api/staff/pools")
    Single<ApiResponse<PoolStaffDto>> createPool(
            @Header("Authorization") String token,
            @Part("name") RequestBody name,
            @Part("address") RequestBody address,
            @Part("geoLat") RequestBody geoLat,
            @Part("geoLng") RequestBody geoLng,
            @Part("description") RequestBody description,
            @Part("openHours") RequestBody openHours,
            @Part MultipartBody.Part image
    );

    @Multipart
    @PUT("api/staff/pools/{id}")
    Single<ApiResponse<PoolStaffDto>> updatePool(
            @Header("Authorization") String token,
            @Path("id") Long id,
            @Part("name") RequestBody name,
            @Part("address") RequestBody address,
            @Part("geoLat") RequestBody geoLat,
            @Part("geoLng") RequestBody geoLng,
            @Part("description") RequestBody description,
            @Part("openHours") RequestBody openHours,
            @Part MultipartBody.Part image
    );

    @PATCH("api/staff/pools/{id}/status")
    Single<ApiResponse<PoolStaffDto>> updatePoolStatus(@Header("Authorization") String token, @Path("id") Long id, @Body java.util.Map<String, String> status);

    @POST("api/staff/pools/{poolId}/slots")
    Single<ApiResponse<SlotStaffDto>> createSlot(@Header("Authorization") String token, @Path("poolId") Long poolId, @Body SlotStaffDto slot);

    @PUT("api/staff/pools/{poolId}/slots/{slotId}")
    Single<ApiResponse<SlotStaffDto>> updateSlot(@Header("Authorization") String token, @Path("poolId") Long poolId, @Path("slotId") Long slotId, @Body SlotStaffDto slot);

    @PATCH("api/staff/pools/{poolId}/slots/{slotId}/status")
    Single<ApiResponse<SlotStaffDto>> updateSlotStatus(@Header("Authorization") String token, @Path("poolId") Long poolId, @Path("slotId") Long slotId, @Body java.util.Map<String, String> status);

    // Public / Shared Operations
    @GET("pools")
    Single<ApiResponse<List<PoolStaffDto>>> getActivePools();

    @GET("pools/{id}")
    Single<ApiResponse<PoolStaffDto>> getPoolDetail(@Path("id") Long id);

    @GET("pools/{poolId}/slots/all")
    Single<ApiResponse<List<SlotStaffDto>>> getPoolSlots(@Path("poolId") Long poolId);

    @GET("pools/{poolId}/slots")
    Single<ApiResponse<List<SlotStaffDto>>> getSlotsByDate(@Path("poolId") Long poolId, @retrofit2.http.Query("date") String date);

    @GET("analytics/revenue")
    Single<ApiResponse<List<RevenueAnalyticsDto>>> getRevenueByTime(
            @Header("Authorization") String token,
            @Query("from") String from,
            @Query("to") String to,
            @Query("granularity") String granularity
    );

    @GET("analytics/revenue/by-pool")
    Single<ApiResponse<List<RevenueAnalyticsDto>>> getRevenueByPool(
            @Header("Authorization") String token,
            @Query("from") String from,
            @Query("to") String to
    );

    @GET("inventory-logs")
    Single<ApiResponse<List<InventoryLogDto>>> getInventoryLogs(
            @Header("Authorization") String token,
            @Query("from") String from,
            @Query("to") String to
    );

    @GET("inventory-logs/slot/{slotId}")
    Single<ApiResponse<List<InventoryLogDto>>> getInventoryLogsBySlot(
            @Header("Authorization") String token,
            @Path("slotId") Long slotId
    );

    @GET("inventory-logs/booking/{bookingCode}")
    Single<ApiResponse<List<InventoryLogDto>>> getInventoryLogsByBooking(
            @Header("Authorization") String token,
            @Path("bookingCode") String bookingCode
    );
}
