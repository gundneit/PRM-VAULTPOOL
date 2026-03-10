package com.vaultpool.customer.domain.repository;

import com.vaultpool.customer.domain.model.Result;
import com.vaultpool.customer.domain.model.User;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

/**
 * Repository interface for Authentication operations.
 * This is part of the Domain Layer - defines the contract without implementation.
 */
public interface AuthRepository {

    /**
     * Login with email and password using Firebase
     * @param email User's email
     * @param password User's password
     * @return Single emitting Result with FirebaseUser
     */
    Single<Result<User>> loginWithEmail(String email, String password);

    /**
     * Register new user with email and password
     * @param email User's email
     * @param password User's password
     * @param fullName User's full name
     * @return Single emitting Result with FirebaseUser
     */
    Single<Result<User>> registerWithEmail(String email, String password, String fullName);

    /**
     * Get currently logged in user
     * @return FirebaseUser or null if not logged in
     */
    Object getCurrentUser();

    /**
     * Check if user is logged in
     * @return true if logged in
     */
    boolean isLoggedIn();

    /**
     * Get Firebase ID token for current user
     * @return Single emitting token string
     */
    Single<String> getIdToken();

    /**
     * Logout current user
     * @return Completable
     */
    Completable logout();

    /**
     * Send password reset email
     * @param email User's email
     * @return Single emitting Result
     */
    Single<Result<Void>> sendPasswordReset(String email);

    /**
     * Reload current user to get latest data
     * @return Single emitting Result
     */
    Single<Result<User>> reloadUser();
}
