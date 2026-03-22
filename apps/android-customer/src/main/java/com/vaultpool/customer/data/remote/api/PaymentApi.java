package com.vaultpool.customer.data.remote.api;

import com.vaultpool.customer.data.remote.dto.ApiResponse;
import com.vaultpool.customer.data.remote.dto.CreatePaymentRequestDto;
import com.vaultpool.customer.data.remote.dto.PaymentResponseDto;
import io.reactivex.rxjava3.core.Single;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface PaymentApi {

    @POST("payments")
    Single<ApiResponse<PaymentResponseDto>> createPayment(
            @Header("Authorization") String token,
            @Body CreatePaymentRequestDto request
    );
}

