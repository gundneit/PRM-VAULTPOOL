package com.vaultpool.customer.data.remote.api;

import com.vaultpool.customer.data.remote.dto.ApiResponse;
import com.vaultpool.customer.data.remote.dto.BookingResponseDto;
import com.vaultpool.customer.data.remote.dto.CreateBookingRequestDto;
import io.reactivex.rxjava3.core.Single;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface BookingApi {

    @POST("bookings")
    Single<ApiResponse<BookingResponseDto>> createBooking(
            @Header("Authorization") String token,
            @Body CreateBookingRequestDto request
    );
}

