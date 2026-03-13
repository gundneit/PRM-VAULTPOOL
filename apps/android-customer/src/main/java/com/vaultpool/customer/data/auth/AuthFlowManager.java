package com.vaultpool.customer.data.auth;

import com.vaultpool.customer.data.local.prefs.PreferencesManager;
import com.vaultpool.customer.domain.model.Result;
import com.vaultpool.customer.domain.model.User;
import com.vaultpool.customer.domain.repository.AuthRepository;

import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.subjects.BehaviorSubject;

/**
 * Manages the authentication flow and session state.
 */
public class AuthFlowManager {

    private final AuthRepository authRepository;
    private final PreferencesManager preferencesManager;
    private final BehaviorSubject<SessionState> sessionSubject = BehaviorSubject.createDefault(SessionState.initial());

    public AuthFlowManager(AuthRepository authRepository, PreferencesManager preferencesManager) {
        this.authRepository = authRepository;
        this.preferencesManager = preferencesManager;
        loadInitialSession();
    }

    private void loadInitialSession() {
        if (preferencesManager.isLoggedIn()) {
            String token = preferencesManager.getFirebaseToken();
            String userId = preferencesManager.getUserId();
            String email = preferencesManager.getUserEmail();
            String name = preferencesManager.getUserName();
            
            User user = User.builder()
                    .id(userId != null ? Long.parseLong(userId) : null)
                    .email(email)
                    .fullName(name)
                    .build();
            
            sessionSubject.onNext(SessionState.authenticated(token, user));
        }
    }

    public BehaviorSubject<SessionState> getSessionState() {
        return sessionSubject;
    }

    public Single<Result<User>> login(String email, String password) {
        sessionSubject.onNext(SessionState.loading());
        return authRepository.loginWithEmail(email, password)
                .map(result -> {
                    if (result.isSuccess()) {
                        User user = result.getData();
                        String token = "mock_token"; // Replace with actual token if available
                        saveSession(token, user);
                        sessionSubject.onNext(SessionState.authenticated(token, user));
                    } else {
                        sessionSubject.onNext(SessionState.unauthenticated());
                    }
                    return result;
                })
                .onErrorReturn(throwable -> {
                    sessionSubject.onNext(SessionState.unauthenticated());
                    return Result.<User>failure(throwable);
                });
    }

    public Single<Result<User>> register(String email, String password, String fullName, String phone) {
        sessionSubject.onNext(SessionState.loading());
        return authRepository.registerWithEmail(email, password, fullName)
                .doOnSuccess(result -> {
                    // Đảm bảo cập nhật lại trạng thái sau khi đăng ký xong
                    sessionSubject.onNext(SessionState.unauthenticated());
                })
                .doOnError(throwable -> {
                    sessionSubject.onNext(SessionState.unauthenticated());
                })
                .onErrorReturn(throwable -> Result.<User>failure(throwable));
    }

    public Single<Result<Void>> logout() {
        return authRepository.logout()
                .andThen(Single.fromCallable(() -> {
                    clearSession();
                    return Result.<Void>success(null);
                }))
                .onErrorReturn(throwable -> Result.<Void>failure(throwable));
    }

    public Single<Result<Void>> sendPasswordReset(String email) {
        return authRepository.sendPasswordReset(email)
                .onErrorReturn(throwable -> Result.<Void>failure(throwable));
    }

    public Single<Result<User>> refreshProfile() {
        return authRepository.reloadUser()
                .onErrorReturn(throwable -> Result.<User>failure(throwable));
    }

    private void saveSession(String token, User user) {
        preferencesManager.setLoggedIn(true);
        preferencesManager.saveFirebaseToken(token);
        if (user != null && user.getId() != null) {
            preferencesManager.saveUserId(String.valueOf(user.getId()));
        }
        if (user != null) {
            preferencesManager.saveUserEmail(user.getEmail());
            preferencesManager.saveUserName(user.getFullName());
        }
    }

    private void clearSession() {
        preferencesManager.clearAll();
        sessionSubject.onNext(SessionState.unauthenticated());
    }

    /**
     * Represents the current state of the authentication session.
     */
    public static class SessionState {
        public enum Status { INITIAL, LOADING, AUTHENTICATED, UNAUTHENTICATED }

        private final Status status;
        private final String token;
        private final User user;

        private SessionState(Status status, String token, User user) {
            this.status = status;
            this.token = token;
            this.user = user;
        }

        public static SessionState initial() {
            return new SessionState(Status.INITIAL, null, null);
        }

        public static SessionState loading() {
            return new SessionState(Status.LOADING, null, null);
        }

        public static SessionState authenticated(String token, User user) {
            return new SessionState(Status.AUTHENTICATED, token, user);
        }

        public static SessionState unauthenticated() {
            return new SessionState(Status.UNAUTHENTICATED, null, null);
        }

        public Status getStatus() {
            return status;
        }

        public String getToken() {
            return token;
        }

        public User getUser() {
            return user;
        }
    }
}
