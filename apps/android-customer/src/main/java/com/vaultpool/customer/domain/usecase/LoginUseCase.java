package com.vaultpool.customer.domain.usecase;

import com.vaultpool.customer.domain.model.Result;
import com.vaultpool.customer.domain.model.User;
import com.vaultpool.customer.domain.repository.AuthRepository;

import io.reactivex.rxjava3.core.Single;

/**
 * UseCase for user login.
 * Contains business logic for login operation.
 */
public class LoginUseCase {

    private final AuthRepository authRepository;

    public LoginUseCase(AuthRepository authRepository) {
        this.authRepository = authRepository;
    }

    /**
     * Execute login with email and password
     * @param email User's email
     * @param password User's password
     * @return Single emitting Result with User
     */
    public Single<Result<User>> execute(String email, String password) {
        // Validation - can be enhanced with more rules
        if (email == null || email.trim().isEmpty()) {
            return Single.just(Result.failure("Email is required"));
        }

        if (password == null || password.trim().isEmpty()) {
            return Single.just(Result.failure("Password is required"));
        }

        if (password.length() < 6) {
            return Single.just(Result.failure("Password must be at least 6 characters"));
        }

        // Delegate to repository
        return authRepository.loginWithEmail(email.trim(), password);
    }

    public AuthRepository getAuthRepository() {
        return authRepository;
    }
}
