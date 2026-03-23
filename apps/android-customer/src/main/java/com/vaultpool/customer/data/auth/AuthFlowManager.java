package com.vaultpool.customer.data.auth;

import com.vaultpool.customer.data.auth.AuthFlowManager.SessionState;
import com.vaultpool.customer.data.local.prefs.PreferencesManager;
import com.vaultpool.customer.data.remote.api.AuthApi;
import com.vaultpool.customer.domain.model.Result;
import com.vaultpool.customer.domain.model.User;
import com.vaultpool.customer.domain.repository.AuthRepository;

import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.BehaviorSubject;
import java.util.Set;

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
            String phone = preferencesManager.getUserPhone();
            Set<String> roles = preferencesManager.getUserRoles();
            
            User user = User.builder()
                    .id(userId != null ? Long.parseLong(userId) : null)
                    .email(email)
                    .fullName(name)
                    .phone(phone)
                    .roles(roles)
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
                .flatMap(result -> {
                    if (result.isSuccess()) {
                        User firebaseUser = result.getData();
                        
                        // Check if email is verified (Status is "ACTIVE" if verified)
                        if (!"ACTIVE".equals(firebaseUser.getStatus())) {
                            return authRepository.logout()
                                    .andThen(Single.just(Result.<User>failure("Please verify your email before logging in.")));
                        }
                        
                        return authRepository.getIdToken()
                                .flatMap(token -> {
                                    if (token == null) return Single.just(Result.success(firebaseUser));
                                    
                                    return authRepository.getProfileFromBackend(token)
                                            .map(profileResult -> {
                                                if (profileResult.isSuccess()) {
                                                    // Backend profile found (might be Staff)
                                                    User backendUser = profileResult.getData();
                                                    saveSession(token, backendUser);
                                                    sessionSubject.onNext(SessionState.authenticated(token, backendUser));
                                                    return Result.success(backendUser);
                                                } else {
                                                    // Backend rejected (403) or not found. 
                                                    // Fallback: Login as a standard Firebase user.
                                                    saveSession(token, firebaseUser);
                                                    sessionSubject.onNext(SessionState.authenticated(token, firebaseUser));
                                                    return Result.success(firebaseUser);
                                                }
                                            })
                                            .onErrorReturn(throwable -> {
                                                // Any network error: still allow login as basic user
                                                saveSession(token, firebaseUser);
                                                sessionSubject.onNext(SessionState.authenticated(token, firebaseUser));
                                                return Result.success(firebaseUser);
                                            });
                                });
                    } else {
                        sessionSubject.onNext(SessionState.unauthenticated());
                        return Single.just(result);
                    }
                })
                .subscribeOn(Schedulers.io())
                .onErrorReturn(throwable -> {
                    sessionSubject.onNext(SessionState.unauthenticated());
                    return Result.<User>failure(throwable);
                });
    }

    public Single<Result<User>> register(String email, String password, String fullName, String phone) {
        sessionSubject.onNext(SessionState.loading());
        return authRepository.registerWithEmail(email, password, fullName, phone)
                .flatMap(result -> {
                    if (result.isSuccess()) {
                        User firebaseUser = result.getData();
                        return authRepository.getIdToken()
                                .flatMap(token -> {
                                    if (token == null) return Single.just(Result.success(firebaseUser));
                                    
                                    AuthApi.RegisterRequest request = new AuthApi.RegisterRequest(
                                            token, email, fullName, phone
                                    );
                                    
                                    // ServiceLocator can be used here or we can inject AuthApi to AuthFlowManager
                                    // For now, let's assume we want to sync with backend
                                    return com.vaultpool.customer.ServiceLocator.getInstance().getAuthApi().register(request)
                                            .map(apiResponse -> {
                                                if (apiResponse.isSuccess()) {
                                                    User backendUser = mapDtoToUser(apiResponse.getData());
                                                    return Result.success(backendUser);
                                                }
                                                return Result.success(firebaseUser); // Fallback to firebase user
                                            })
                                            .onErrorReturn(throwable -> Result.success(firebaseUser));
                                });
                    }
                    return Single.just(result);
                })
                .doOnSuccess(result -> {
                    sessionSubject.onNext(SessionState.unauthenticated());
                })
                .doOnError(throwable -> {
                    sessionSubject.onNext(SessionState.unauthenticated());
                })
                .subscribeOn(Schedulers.io());
    }

    private User mapDtoToUser(com.vaultpool.customer.data.remote.dto.UserDto dto) {
        User user = new User();
        user.setId(dto.getId());
        user.setFirebaseUid(dto.getFirebaseUid());
        user.setFullName(dto.getFullName());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        user.setStatus(dto.getStatus());
        user.setRoles(dto.getRoles());
        return user;
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
        return authRepository.getIdToken()
                .flatMap(token -> {
                    if (token == null) return Single.just(Result.<User>failure("No token"));
                    
                    return authRepository.getProfileFromBackend(token)
                            .flatMap(profileResult -> {
                                if (profileResult.isSuccess()) {
                                    User user = profileResult.getData();
                                    saveSession(token, user);
                                    sessionSubject.onNext(SessionState.authenticated(token, user));
                                    return Single.just(Result.success(user));
                                } else {
                                    // Backend profile failed (could be 403 for regular users).
                                    // Fallback to reloading the basic Firebase profile.
                                    return authRepository.reloadUser()
                                            .map(reloadResult -> {
                                                if (reloadResult.isSuccess()) {
                                                    User user = reloadResult.getData();
                                                    saveSession(token, user);
                                                    sessionSubject.onNext(SessionState.authenticated(token, user));
                                                    // Return success even if backend call failed,
                                                    // as long as we have a valid Firebase user.
                                                    return Result.success(user);
                                                } else {
                                                    return reloadResult;
                                                }
                                            })
                                            .onErrorReturn(throwable -> Result.failure(throwable));
                                }
                            })
                            .onErrorResumeNext(throwable -> {
                                // If even the network call failed, try Firebase reload
                                return authRepository.reloadUser()
                                        .map(reloadResult -> {
                                            if (reloadResult.isSuccess()) {
                                                User user = reloadResult.getData();
                                                saveSession(token, user);
                                                sessionSubject.onNext(SessionState.authenticated(token, user));
                                                return Result.success(user);
                                            } else {
                                                return Result.<User>failure(throwable);
                                            }
                                        });
                            });
                })
                .subscribeOn(Schedulers.io());
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
            preferencesManager.saveUserPhone(user.getPhone());
            if (user.getRoles() != null) {
                preferencesManager.saveUserRoles(user.getRoles());
            }
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
