package com.vaultpool.customer.presentation.ui.auth;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.vaultpool.customer.domain.model.Result;
import com.vaultpool.customer.domain.model.User;
import com.vaultpool.customer.domain.repository.AuthRepository;
import com.vaultpool.customer.domain.usecase.LoginUseCase;
import com.vaultpool.customer.presentation.state.AuthUiState;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * ViewModel for Authentication screens.
 * Handles business logic and manages UI state.
 */
public class AuthViewModel extends ViewModel {

    private final LoginUseCase loginUseCase;
    private final AuthRepository authRepository;
    private final CompositeDisposable disposables = new CompositeDisposable();

    private final MutableLiveData<AuthUiState> uiState =
            new MutableLiveData<>(AuthUiState.Idle.getInstance());

    public AuthViewModel(LoginUseCase loginUseCase) {
        this.loginUseCase = loginUseCase;
        this.authRepository = loginUseCase.getAuthRepository();
    }

    public LiveData<AuthUiState> getUiState() {
        return uiState;
    }

    /**
     * Login with email and password
     */
    public void login(String email, String password) {
        uiState.setValue(AuthUiState.Loading.getInstance());

        disposables.add(
                loginUseCase.execute(email, password)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(result -> {
                            if (result.isSuccess()) {
                                // Reload user to check email verification
                                checkEmailVerification(result.getData());
                            } else {
                                uiState.setValue(new AuthUiState.Error(
                                        getErrorMessage(result.getError())
                                ));
                            }
                        })
        );
    }

    /**
     * Register new user
     */
    public void register(String email, String password, String fullName) {
        uiState.setValue(AuthUiState.Loading.getInstance());

        disposables.add(
                authRepository.registerWithEmail(email, password, fullName)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(result -> {
                            if (result.isSuccess()) {
                                uiState.setValue(new AuthUiState.RegisterSuccess(
                                        "Đăng ký thành công. Vui lòng kiểm tra email để xác thực.",
                                        result.getData()
                                ));
                            } else {
                                uiState.setValue(new AuthUiState.Error(
                                        getErrorMessage(result.getError())
                                ));
                            }
                        })
        );
    }

    /**
     * Resend verification email
     */
    public void resendVerificationEmail() {
        uiState.setValue(AuthUiState.Loading.getInstance());

        disposables.add(
                authRepository.reloadUser()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(result -> {
                            // Note: Firebase Auth doesn't have API to resend verification
                            // This would need to be implemented via backend
                            uiState.setValue(new AuthUiState.Error(
                                    "Tính năng đang được phát triển"
                            ));
                        })
        );
    }

    /**
     * Check if email is verified
     */
    public void checkEmailVerification() {
        uiState.setValue(AuthUiState.Loading.getInstance());

        disposables.add(
                authRepository.reloadUser()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(result -> {
                            if (result.isSuccess()) {
                                User user = result.getData();
                                if (user != null && "ACTIVE".equals(user.getStatus())) {
                                    uiState.setValue(new AuthUiState.EmailVerified(user));
                                } else {
                                    uiState.setValue(new AuthUiState.EmailNotVerified(
                                            "Email chưa được xác thực.",
                                            user
                                    ));
                                }
                            } else {
                                uiState.setValue(new AuthUiState.Error(
                                        getErrorMessage(result.getError())
                                ));
                            }
                        })
        );
    }

    /**
     * Check email verification after login
     */
    private void checkEmailVerification(User user) {
        disposables.add(
                authRepository.reloadUser()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(result -> {
                            if (result.isSuccess()) {
                                User reloadedUser = result.getData();
                                if (reloadedUser != null && "ACTIVE".equals(reloadedUser.getStatus())) {
                                    uiState.setValue(new AuthUiState.LoginSuccess(reloadedUser));
                                } else {
                                    uiState.setValue(new AuthUiState.EmailNotVerified(
                                            "Vui lòng xác thực email trước khi đăng nhập.",
                                            reloadedUser
                                    ));
                                }
                            } else {
                                // If reload fails but login was successful, still allow login
                                uiState.setValue(new AuthUiState.LoginSuccess(user));
                            }
                        })
        );
    }

    /**
     * Reset state to idle
     */
    public void resetState() {
        uiState.setValue(AuthUiState.Idle.getInstance());
    }

    /**
     * Check if user is already logged in
     */
    public boolean isLoggedIn() {
        return authRepository.isLoggedIn();
    }

    /**
     * Get error message from throwable
     */
    private String getErrorMessage(Throwable throwable) {
        String message = throwable != null ? throwable.getMessage() : null;
        if (message == null) {
            return "Đã xảy ra lỗi. Vui lòng thử lại sau.";
        }

        // Map Firebase error messages to Vietnamese
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
                return message;
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        disposables.clear();
    }
}
