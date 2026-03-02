package com.lavelahotel.poolbooking.ui.auth;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseUser;
import com.lavelahotel.poolbooking.data.repository.FirebaseAuthRepository;

public class FirebaseAuthViewModel extends ViewModel {

    private final FirebaseAuthRepository repository;
    private final MutableLiveData<AuthUiState> uiState =
            new MutableLiveData<>(AuthUiState.Idle.INSTANCE);

    public FirebaseAuthViewModel(FirebaseAuthRepository repository) {
        this.repository = repository;
    }

    public LiveData<AuthUiState> getUiState() {
        return uiState;
    }

    public void register(String email, String password, String fullName) {
        uiState.setValue(AuthUiState.Loading.INSTANCE);

        repository.registerWithEmail(email, password, fullName, new FirebaseAuthRepository.AuthCallback() {
            @Override
            public void onSuccess(FirebaseUser user) {
                uiState.postValue(new AuthUiState.RegisterSuccess(
                        "Đăng ký thành công. Vui lòng kiểm tra email để xác thực.",
                        user
                ));
            }

            @Override
            public void onError(Throwable t) {
                uiState.postValue(new AuthUiState.Error(getErrorMessage(t)));
            }
        });
    }

    public void login(String email, String password) {
        uiState.setValue(AuthUiState.Loading.INSTANCE);

        repository.loginWithEmail(email, password, new FirebaseAuthRepository.AuthCallback() {
            @Override
            public void onSuccess(FirebaseUser user) {
                repository.reloadUser(new FirebaseAuthRepository.AuthCallback() {
                    @Override
                    public void onSuccess(FirebaseUser reloadedUser) {
                        if (reloadedUser.isEmailVerified()) {
                            uiState.postValue(new AuthUiState.LoginSuccess(reloadedUser));
                        } else {
                            uiState.postValue(new AuthUiState.EmailNotVerified(
                                    "Vui lòng xác thực email trước khi đăng nhập.",
                                    reloadedUser
                            ));
                        }
                    }

                    @Override
                    public void onError(Throwable t) {
                        // Nếu reload thất bại nhưng login thành công, vẫn cho login
                        uiState.postValue(new AuthUiState.LoginSuccess(user));
                    }
                });
            }

            @Override
            public void onError(Throwable t) {
                uiState.postValue(new AuthUiState.Error(getErrorMessage(t)));
            }
        });
    }

    public void resendVerificationEmail() {
        uiState.setValue(AuthUiState.Loading.INSTANCE);
        repository.resendVerificationEmail(new FirebaseAuthRepository.SimpleCallback() {
            @Override
            public void onSuccess() {
                uiState.postValue(new AuthUiState.EmailSent(
                        "Email xác thực đã được gửi. Vui lòng kiểm tra hộp thư."
                ));
            }

            @Override
            public void onError(Throwable t) {
                uiState.postValue(new AuthUiState.Error(getErrorMessage(t)));
            }
        });
    }

    public void checkEmailVerification() {
        repository.reloadUser(new FirebaseAuthRepository.AuthCallback() {
            @Override
            public void onSuccess(FirebaseUser user) {
                if (user.isEmailVerified()) {
                    uiState.postValue(new AuthUiState.EmailVerified(user));
                } else {
                    uiState.postValue(new AuthUiState.EmailNotVerified(
                            "Email chưa được xác thực.",
                            user
                    ));
                }
            }

            @Override
            public void onError(Throwable t) {
                uiState.postValue(new AuthUiState.Error(getErrorMessage(t)));
            }
        });
    }

    public void resetState() {
        uiState.setValue(AuthUiState.Idle.INSTANCE);
    }

    private String getErrorMessage(Throwable throwable) {
        String message = throwable != null ? throwable.getMessage() : null;
        if (message == null) {
            return "Đã xảy ra lỗi. Vui lòng thử lại sau.";
        }

        switch (message) {
            case "The email address is already in use by another account.":
                return "Email này đã được sử dụng. Vui lòng sử dụng email khác.";
            case "The password is too weak.":
                return "Mật khẩu quá yếu. Vui lòng sử dụng mật khẩu mạnh hơn.";
            case "The email address is badly formatted.":
                return "Email không hợp lệ.";
            case "There is no user record corresponding to this identifier.":
                return "Email hoặc mật khẩu không đúng.";
            case "The password is invalid or the user does not have a password.":
                return "Mật khẩu không đúng.";
            case "A network error (such as timeout, interrupted connection or unreachable host) has occurred.":
                return "Lỗi kết nối. Vui lòng kiểm tra internet và thử lại.";
            default:
                return message != null ? message : "Đã xảy ra lỗi. Vui lòng thử lại sau.";
        }
    }
}

