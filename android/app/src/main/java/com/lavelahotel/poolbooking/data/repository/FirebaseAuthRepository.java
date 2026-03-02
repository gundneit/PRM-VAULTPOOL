package com.lavelahotel.poolbooking.data.repository;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class FirebaseAuthRepository {

    private final FirebaseAuth auth = FirebaseAuth.getInstance();

    public interface AuthCallback {
        void onSuccess(FirebaseUser user);
        void onError(Throwable t);
    }

    public interface SimpleCallback {
        void onSuccess();
        void onError(Throwable t);
    }

    public void registerWithEmail(String email,
                                  String password,
                                  String fullName,
                                  final AuthCallback callback) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            if (callback != null) {
                                callback.onError(task.getException() != null
                                        ? task.getException()
                                        : new Exception("User creation failed"));
                            }
                            return;
                        }

                        FirebaseUser user = task.getResult() != null ? task.getResult().getUser() : null;
                        if (user == null) {
                            if (callback != null) {
                                callback.onError(new Exception("User creation failed"));
                            }
                            return;
                        }

                        UserProfileChangeRequest profileUpdates =
                                new UserProfileChangeRequest.Builder()
                                        .setDisplayName(fullName)
                                        .build();

                        user.updateProfile(profileUpdates)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        // Dù update profile thành công hay không, vẫn tiếp tục gửi email verify
                                        user.sendEmailVerification()
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> emailTask) {
                                                        if (emailTask.isSuccessful()) {
                                                            if (callback != null) {
                                                                callback.onSuccess(user);
                                                            }
                                                        } else {
                                                            if (callback != null) {
                                                                callback.onError(emailTask.getException() != null
                                                                        ? emailTask.getException()
                                                                        : new Exception("Send verification email failed"));
                                                            }
                                                        }
                                                    }
                                                });
                                    }
                                });
                    }
                });
    }

    public void loginWithEmail(String email,
                               String password,
                               final AuthCallback callback) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = task.getResult() != null ? task.getResult().getUser() : null;
                            if (user != null) {
                                if (callback != null) {
                                    callback.onSuccess(user);
                                }
                            } else if (callback != null) {
                                callback.onError(new Exception("Login failed"));
                            }
                        } else if (callback != null) {
                            callback.onError(task.getException() != null
                                    ? task.getException()
                                    : new Exception("Login failed"));
                        }
                    }
                });
    }

    public boolean isEmailVerified() {
        FirebaseUser user = auth.getCurrentUser();
        return user != null && user.isEmailVerified();
    }

    public void reloadUser(final AuthCallback callback) {
        final FirebaseUser current = auth.getCurrentUser();
        if (current == null) {
            if (callback != null) {
                callback.onError(new Exception("No user logged in"));
            }
            return;
        }

        current.reload().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    FirebaseUser refreshed = auth.getCurrentUser();
                    if (refreshed != null) {
                        if (callback != null) {
                            callback.onSuccess(refreshed);
                        }
                    } else if (callback != null) {
                        callback.onError(new Exception("No user after reload"));
                    }
                } else if (callback != null) {
                    callback.onError(task.getException() != null
                            ? task.getException()
                            : new Exception("Reload user failed"));
                }
            }
        });
    }

    public void resendVerificationEmail(final SimpleCallback callback) {
        final FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            if (callback != null) {
                callback.onError(new Exception("No user logged in"));
            }
            return;
        }

        user.sendEmailVerification()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            if (callback != null) {
                                callback.onSuccess();
                            }
                        } else if (callback != null) {
                            callback.onError(task.getException() != null
                                    ? task.getException()
                                    : new Exception("Resend verification email failed"));
                        }
                    }
                });
    }

    public FirebaseUser getCurrentUser() {
        return auth.getCurrentUser();
    }

    public void logout() {
        auth.signOut();
    }

    public void sendPasswordResetEmail(String email, final SimpleCallback callback) {
        auth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            if (callback != null) {
                                callback.onSuccess();
                            }
                        } else if (callback != null) {
                            callback.onError(task.getException() != null
                                    ? task.getException()
                                    : new Exception("Send password reset email failed"));
                        }
                    }
                });
    }
}

