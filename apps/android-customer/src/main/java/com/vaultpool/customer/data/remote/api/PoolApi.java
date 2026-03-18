package com.vaultpool.customer.data.remote.api;

import com.vaultpool.customer.data.remote.dto.PoolDto;
import com.vaultpool.customer.data.remote.dto.SlotDto;
import com.vaultpool.models.ApiResponse;
import java.util.List;
import io.reactivex.rxjava3.core.Single;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface PoolApi {

    @GET("pools")
    Single<ApiResponse<List<PoolDto>>> getActivePools();

    @GET("pools/{id}")
    Single<ApiResponse<PoolDto>> getPoolDetail(@Path("id") Long id);

    @GET("pools/{id}/slots")
    Single<ApiResponse<List<SlotDto>>> getSlotsByPoolAndDate(
            @Path("id") Long id,
            @Query("date") String date
    );

    @GET("pools/{id}/slots/all")
    Single<ApiResponse<List<SlotDto>>> getAllSlotsByPoolId(@Path("id") Long id);
}
