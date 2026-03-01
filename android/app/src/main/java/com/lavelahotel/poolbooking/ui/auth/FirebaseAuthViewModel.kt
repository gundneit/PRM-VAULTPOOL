package com.lavelahotel.poolbooking.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.lavelahotel.poolbooking.data.repository.FirebaseAuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FirebaseAuthViewModel(
    private val repository: FirebaseAuthRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()
    
    fun register(email: String, password: String, fullName: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            repository.registerWithEmail(email, password, fullName)
                .onSuccess { user ->
                    _uiState.value = AuthUiState.RegisterSuccess(
                        message = "Đăng ký thành công. Vui lòng kiểm tra email để xác thực.",
                        user = user
                    )
                }
                .onFailure {
                    _uiState.value = AuthUiState.Error(getErrorMessage(it))
                }
        }
    }
    
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            repository.loginWithEmail(email, password)
                .onSuccess { user ->
                    // Reload user để check emailVerified status
                    repository.reloadUser()
                        .onSuccess { reloadedUser ->
                            if (reloadedUser.isEmailVerified) {
                                _uiState.value = AuthUiState.LoginSuccess(reloadedUser)
                            } else {
                                _uiState.value = AuthUiState.EmailNotVerified(
                                    message = "Vui lòng xác thực email trước khi đăng nhập.",
                                    user = reloadedUser
                                )
                            }
                        }
                        .onFailure {
                            _uiState.value = AuthUiState.LoginSuccess(user)
                        }
                }
                .onFailure {
                    _uiState.value = AuthUiState.Error(getErrorMessage(it))
                }
        }
    }
    
    fun resendVerificationEmail() {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            repository.resendVerificationEmail()
                .onSuccess {
                    _uiState.value = AuthUiState.EmailSent(
                        message = "Email xác thực đã được gửi. Vui lòng kiểm tra hộp thư."
                    )
                }
                .onFailure {
                    _uiState.value = AuthUiState.Error(getErrorMessage(it))
                }
        }
    }
    
    fun checkEmailVerification() {
        viewModelScope.launch {
            repository.reloadUser()
                .onSuccess { user ->
                    if (user.isEmailVerified) {
                        _uiState.value = AuthUiState.EmailVerified(user)
                    } else {
                        _uiState.value = AuthUiState.EmailNotVerified(
                            message = "Email chưa được xác thực.",
                            user = user
                        )
                    }
                }
                .onFailure {
                    _uiState.value = AuthUiState.Error(getErrorMessage(it))
                }
        }
    }
    
    fun resetState() {
        _uiState.value = AuthUiState.Idle
    }
    
    private fun getErrorMessage(throwable: Throwable): String {
        return when (throwable.message) {
            "The email address is already in use by another account." -> 
                "Email này đã được sử dụng. Vui lòng sử dụng email khác."
            "The password is too weak." -> 
                "Mật khẩu quá yếu. Vui lòng sử dụng mật khẩu mạnh hơn."
            "The email address is badly formatted." -> 
                "Email không hợp lệ."
            "There is no user record corresponding to this identifier." -> 
                "Email hoặc mật khẩu không đúng."
            "The password is invalid or the user does not have a password." -> 
                "Mật khẩu không đúng."
            "A network error (such as timeout, interrupted connection or unreachable host) has occurred." -> 
                "Lỗi kết nối. Vui lòng kiểm tra internet và thử lại."
            else -> throwable.message ?: "Đã xảy ra lỗi. Vui lòng thử lại sau."
        }
    }
}

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    data class RegisterSuccess(val message: String, val user: FirebaseUser) : AuthUiState()
    data class LoginSuccess(val user: FirebaseUser) : AuthUiState()
    data class EmailNotVerified(val message: String, val user: FirebaseUser) : AuthUiState()
    data class EmailVerified(val user: FirebaseUser) : AuthUiState()
    data class EmailSent(val message: String) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}
