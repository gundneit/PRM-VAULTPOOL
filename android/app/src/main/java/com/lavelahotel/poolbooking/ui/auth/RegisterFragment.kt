package com.lavelahotel.poolbooking.ui.auth

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.lavelahotel.poolbooking.R
import com.lavelahotel.poolbooking.databinding.FragmentRegisterBinding
import com.lavelahotel.poolbooking.data.repository.FirebaseAuthRepository
import com.lavelahotel.poolbooking.util.ValidationUtils
import kotlinx.coroutines.launch

class RegisterFragment : Fragment() {
    
    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: FirebaseAuthViewModel by activityViewModels {
        FirebaseAuthViewModelFactory(
            FirebaseAuthRepository()
        )
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupClickListeners()
        observeViewModel()
    }
    
    private fun setupClickListeners() {
        binding.btnRegister.setOnClickListener {
            if (validateInput()) {
                val selectedChannelId = binding.rgVerificationChannel.checkedRadioButtonId

                when (selectedChannelId) {
                    R.id.rbEmail -> {
                        // Đăng ký với Email + Password (Firebase email/password auth)
                        val email = binding.etEmail.text.toString().trim()
                        val fullName = binding.etFullName.text.toString().trim()
                        val password = binding.etPassword.text.toString()
                        viewModel.register(email, password, fullName)
                    }
                    R.id.rbSms -> {
                        val phone = binding.etPhone.text.toString().trim()
                        // Chuyển sang màn hình nhập OTP SMS (Firebase Phone Auth)
                        (activity as? AuthActivity)?.navigateToPhoneOtp(phone)
                    }
                    else -> {
                        Toast.makeText(
                            context,
                            "Vui lòng chọn phương thức nhận mã (Email hoặc SMS).",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
        
        binding.tvLoginLink.setOnClickListener {
            (activity as? AuthActivity)?.navigateToLogin()
        }
    }
    
    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is AuthUiState.Loading -> {
                        showLoading(true)
                    }
                    is AuthUiState.RegisterSuccess -> {
                        showLoading(false)
                        Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                        // Sau khi đăng ký thành công: quay về màn hình đăng nhập
                        (activity as? AuthActivity)?.navigateToLogin()
                        // Reset state để tránh lặp lại event khi quay lại màn hình
                        viewModel.resetState()
                    }
                    is AuthUiState.Error -> {
                        showLoading(false)
                        showError(state.message)
                    }
                    else -> {
                        showLoading(false)
                    }
                }
            }
        }
    }
    
    private fun validateInput(): Boolean {
        var isValid = true
        
        val fullName = binding.etFullName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()
        val confirmPassword = binding.etConfirmPassword.text.toString()
        
        if (TextUtils.isEmpty(fullName)) {
            binding.tilFullName.error = getString(R.string.error_required)
            isValid = false
        } else {
            binding.tilFullName.error = null
        }
        
        if (TextUtils.isEmpty(email)) {
            binding.tilEmail.error = getString(R.string.error_required)
            isValid = false
        } else if (!ValidationUtils.isValidEmail(email)) {
            binding.tilEmail.error = getString(R.string.error_invalid_email)
            isValid = false
        } else {
            binding.tilEmail.error = null
        }
        
        // Phone is optional with Firebase
        val phone = binding.etPhone.text.toString().trim()
        if (phone.isNotEmpty() && !ValidationUtils.isValidPhone(phone)) {
            binding.tilPhone.error = getString(R.string.error_invalid_phone)
            isValid = false
        } else {
            binding.tilPhone.error = null
        }
        
        if (TextUtils.isEmpty(password)) {
            binding.tilPassword.error = getString(R.string.error_required)
            isValid = false
        } else if (password.length < 6) { // Firebase minimum is 6
            binding.tilPassword.error = "Mật khẩu phải có ít nhất 6 ký tự"
            isValid = false
        } else {
            binding.tilPassword.error = null
        }
        
        if (TextUtils.isEmpty(confirmPassword)) {
            binding.tilConfirmPassword.error = getString(R.string.error_required)
            isValid = false
        } else if (password != confirmPassword) {
            binding.tilConfirmPassword.error = getString(R.string.error_password_mismatch)
            isValid = false
        } else {
            binding.tilConfirmPassword.error = null
        }

        // Người dùng phải chọn kênh nhận mã (email / sms)
        if (binding.rgVerificationChannel.checkedRadioButtonId == -1) {
            Toast.makeText(
                context,
                "Vui lòng chọn phương thức nhận mã (Email hoặc SMS).",
                Toast.LENGTH_LONG
            ).show()
            isValid = false
        }

        return isValid
    }
    
    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.btnRegister.isEnabled = !show
    }
    
    private fun showError(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
