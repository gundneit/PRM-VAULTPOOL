package com.vaultpool.customer.data.remote.api;

import com.vaultpool.customer.data.remote.dto.ApiResponse;
import com.vaultpool.customer.data.remote.dto.UserDto;

import io.reactivex.rxjava3.core.Single;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;

/**
 * Retrofit API interface for Auth endpoints.
 */
public interface AuthApi {

    /**
     * Register new user with Firebase ID token
     */
    @POST("api/auth/register")
    Single<ApiResponse<UserDto>> register(
            @Body RegisterRequest request
    );

    /**
     * Get current user profile
     */
    @GET("api/auth/me")
    Single<ApiResponse<UserDto>> getProfile(
            @Header("Authorization") String bearerToken
    );

    /**
     * Request class for register endpoint
     */
    class RegisterRequest {
        private String firebaseIdToken;
        private String email;
        private String fullName;
        private String phone;

        public RegisterRequest() {
        }

        public RegisterRequest(String firebaseIdToken, String email, String fullName, String phone) {
            this.firebaseIdToken = firebaseIdToken;
            this.email = email;
            this.fullName = fullName;
            this.phone = phone;
        }

        public String getFirebaseIdToken() {
            return firebaseIdToken;
        }

        public void setFirebaseIdToken(String firebaseIdToken) {
            this.firebaseIdToken = firebaseIdToken;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getFullName() {
            return fullName;
        }

        public void setFullName(String fullName) {
            this.fullName = fullName;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }
    }
}
