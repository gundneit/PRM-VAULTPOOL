package com.lavelahotel.poolbooking.ui.auth;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.lavelahotel.poolbooking.data.repository.FirebaseAuthRepository;

public class FirebaseAuthViewModelFactory implements ViewModelProvider.Factory {

    private final FirebaseAuthRepository repository;

    public FirebaseAuthViewModelFactory(FirebaseAuthRepository repository) {
        this.repository = repository;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(FirebaseAuthViewModel.class)) {
            return (T) new FirebaseAuthViewModel(repository);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}

