package com.vaultpool.customer.data.repository;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.vaultpool.customer.data.remote.api.AuthApi;
import com.vaultpool.customer.data.remote.dto.UserDto;
import com.vaultpool.customer.domain.model.Result;
import com.vaultpool.customer.domain.model.User;
import com.vaultpool.customer.domain.repository.AuthRepository;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class FirebaseAuthRepository implements AuthRepository {

    private final FirebaseAuth auth;
    private final AuthApi authApi;

    public FirebaseAuthRepository(AuthApi authApi) {
        this.auth = FirebaseAuth.getInstance();
        this.authApi = authApi;
    }

    @Override
    public Single<Result<User>> loginWithEmail(String email, String password) {
        return Single.create(emitter -> {
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = task.getResult().getUser();
                        if (firebaseUser != null) {
                            User user = mapFirebaseUserToUser(firebaseUser);
                            emitter.onSuccess(Result.success(user));
                        } else {
                            emitter.onSuccess(Result.failure("User is null"));
                        }
                    } else {
                        emitter.onSuccess(Result.failure(task.getException() != null ? task.getException() : new Exception("Login failed")));
                    }
                });
        });
    }

    @Override
    public Single<Result<User>> registerWithEmail(String email, String password, String fullName, String phone) {
        return Single.create(emitter -> {
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        emitter.onSuccess(Result.failure(task.getException() != null ? task.getException() : new Exception("Registration failed")));
                        return;
                    }
                    FirebaseUser firebaseUser = task.getResult().getUser();
                    if (firebaseUser == null) {
                        emitter.onSuccess(Result.failure("User is null"));
                        return;
                    }
                    UserProfileChangeRequest updates = new UserProfileChangeRequest.Builder()
                        .setDisplayName(fullName)
                        .build();
                    firebaseUser.updateProfile(updates)
                        .addOnCompleteListener(profileTask -> {
                            firebaseUser.sendEmailVerification()
                                .addOnCompleteListener(emailTask -> {
                                    User user = mapFirebaseUserToUser(firebaseUser);
                                    // Manually set phone as Firebase doesn't allow setting it here
                                    user.setPhone(phone);
                                    emitter.onSuccess(Result.success(user));
                                });
                        });
                });
        });
    }

    @Override
    public Object getCurrentUser() {
        return auth.getCurrentUser();
    }

    @Override
    public boolean isLoggedIn() {
        return auth.getCurrentUser() != null;
    }

    @Override
    public Single<String> getIdToken() {
        return Single.create(emitter -> {
            FirebaseUser user = auth.getCurrentUser();
            if (user == null) {
                emitter.onSuccess(null);
                return;
            }
            user.getIdToken(true)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        emitter.onSuccess(task.getResult().getToken());
                    } else {
                        emitter.onSuccess(null);
                    }
                });
        });
    }

    @Override
    public Completable logout() {
        return Completable.create(emitter -> {
            auth.signOut();
            emitter.onComplete();
        });
    }

    @Override
    public Single<Result<Void>> sendPasswordReset(String email) {
        return Single.create(emitter -> {
            auth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        emitter.onSuccess(Result.success(null));
                    } else {
                        emitter.onSuccess(Result.failure(task.getException() != null ? task.getException() : new Exception("Failed to send reset email")));
                    }
                });
        });
    }

    @Override
    public Single<Result<User>> reloadUser() {
        return Single.create(emitter -> {
            FirebaseUser current = auth.getCurrentUser();
            if (current == null) {
                emitter.onSuccess(Result.failure("No user logged in"));
                return;
            }
            current.reload()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser refreshed = auth.getCurrentUser();
                        if (refreshed != null) {
                            emitter.onSuccess(Result.success(mapFirebaseUserToUser(refreshed)));
                        } else {
                            emitter.onSuccess(Result.failure("User is null after reload"));
                        }
                    } else {
                        emitter.onSuccess(Result.failure(task.getException() != null ? task.getException() : new Exception("Reload failed")));
                    }
                });
        });
    }

    @Override
    public Single<Result<User>> getProfileFromBackend(String token) {
        String bearerToken = token.startsWith("Bearer ") ? token : "Bearer " + token;
        return authApi.getProfile(bearerToken)
                .subscribeOn(Schedulers.io())
                .map(response -> {
                    if (response.isSuccess() && response.getData() != null) {
                        return Result.success(mapDtoToUser(response.getData()));
                    } else {
                        return Result.<User>failure(response.getMessage());
                    }
                })
                .onErrorReturn(Result::failure);
    }

    private User mapFirebaseUserToUser(FirebaseUser firebaseUser) {
        User user = new User();
        user.setFirebaseUid(firebaseUser.getUid());
        user.setEmail(firebaseUser.getEmail());
        user.setFullName(firebaseUser.getDisplayName());
        user.setPhone(firebaseUser.getPhoneNumber());
        user.setStatus(firebaseUser.isEmailVerified() ? "ACTIVE" : "PENDING");
        return user;
    }

    private User mapDtoToUser(UserDto dto) {
        User user = new User();
        user.setId(dto.getId());
        user.setFirebaseUid(dto.getFirebaseUid());
        user.setFullName(dto.getFullName());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        user.setStatus(dto.getStatus());
        user.setRoles(dto.getRoles());
        user.setCreatedAt(dto.getCreatedAt());
        user.setUpdatedAt(dto.getUpdatedAt());
        return user;
    }
}
