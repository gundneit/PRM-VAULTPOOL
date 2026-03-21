package com.vaultpool.customer.presentation.ui.staff;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.vaultpool.customer.domain.repository.AuthRepository;
import com.vaultpool.customer.domain.repository.StaffRepository;

public class StaffViewModelFactory implements ViewModelProvider.Factory {
    private final StaffRepository staffRepository;
    private final AuthRepository authRepository;

    public StaffViewModelFactory(StaffRepository staffRepository, AuthRepository authRepository) {
        this.staffRepository = staffRepository;
        this.authRepository = authRepository;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(StaffViewModel.class)) {
            return (T) new StaffViewModel(staffRepository, authRepository);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
