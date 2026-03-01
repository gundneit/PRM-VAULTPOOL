package com.lavelahotel.poolbooking.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.lavelahotel.poolbooking.data.repository.FirebaseAuthRepository

class FirebaseAuthViewModelFactory(
    private val repository: FirebaseAuthRepository
) : ViewModelProvider.Factory {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FirebaseAuthViewModel::class.java)) {
            return FirebaseAuthViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
