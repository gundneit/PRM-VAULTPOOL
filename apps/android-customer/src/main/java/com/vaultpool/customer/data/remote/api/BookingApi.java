package com.vaultpool.customer.data.remote.api;

import androidx.annotation.Nullable;

import com.vaultpool.customer.data.remote.dto.ApiResponse;
import com.vaultpool.customer.data.remote.dto.BookingResponseDto;
import com.vaultpool.customer.data.remote.dto.CartCountDto;
import com.vaultpool.customer.data.remote.dto.CreateBookingRequestDto;
import io.reactivex.rxjava3.core.Single;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

import java.util.List;

public interface BookingApi {

    @GET("bookings")
    Single<ApiResponse<List<BookingResponseDto>>> getMyBookings(
            @Header("Authorization") String token,
            @Query("status") @Nullable String status
    );

    @POST("bookings")
    Single<ApiResponse<BookingResponseDto>> createBooking(
            @Header("Authorization") String token,
            @Body CreateBookingRequestDto request
    );

    @GET("bookings/cart")
    Single<ApiResponse<List<BookingResponseDto>>> getCart(
            @Header("Authorization") String token
    );

    @GET("cart/count")
    Single<ApiResponse<CartCountDto>> getCartCount(
            @Header("Authorization") String token
    );

    @POST("bookings/{id}/checkout")
    Single<ApiResponse<BookingResponseDto>> checkoutBooking(
            @Header("Authorization") String token,
            @Path("id") Long id
    );
}
