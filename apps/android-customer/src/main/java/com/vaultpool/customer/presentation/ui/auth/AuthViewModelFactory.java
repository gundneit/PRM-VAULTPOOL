package com.vaultpool.customer.presentation.ui.auth;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.vaultpool.customer.domain.usecase.LoginUseCase;

/**
 * Factory for creating AuthViewModel with dependencies.
 */
public class AuthViewModelFactory implements ViewModelProvider.Factory {

    private final LoginUseCase loginUseCase;

    public AuthViewModelFactory(LoginUseCase loginUseCase) {
        this.loginUseCase = loginUseCase;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(AuthViewModel.class)) {
            return (T) new AuthViewModel(loginUseCase);
        }
        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
    }
}
