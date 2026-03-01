package com.lavelahotel.poolbooking.ui.auth

import android.content.Intent
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
import com.lavelahotel.poolbooking.databinding.FragmentLoginBinding
import com.lavelahotel.poolbooking.data.repository.FirebaseAuthRepository
import com.lavelahotel.poolbooking.ui.main.MainActivity
import com.lavelahotel.poolbooking.util.ValidationUtils
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {
    
    private var _binding: FragmentLoginBinding? = null
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
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupClickListeners()
        observeViewModel()
    }
    
    private fun setupClickListeners() {
        binding.btnLogin.setOnClickListener {
            if (validateInput()) {
                val emailOrPhone = binding.etEmailOrPhone.text.toString().trim()
                val password = binding.etPassword.text.toString()
                viewModel.login(emailOrPhone, password)
            }
        }
        
        binding.tvRegisterLink.setOnClickListener {
            (activity as? AuthActivity)?.navigateToRegister()
        }
        
        binding.tvForgotPassword.setOnClickListener {
            // TODO: Navigate to forgot password screen
            Toast.makeText(context, "Tính năng đang phát triển", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is AuthUiState.Loading -> {
                        showLoading(true)
                    }
                    is AuthUiState.LoginSuccess -> {
                        showLoading(false)
                        Toast.makeText(context, getString(R.string.login_success), Toast.LENGTH_SHORT).show()
                        navigateToMain()
                    }
                    is AuthUiState.EmailNotVerified -> {
                        showLoading(false)
                        Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                        // Navigate to email verification screen
                        (activity as? AuthActivity)?.navigateToEmailVerification(state.user.email ?: "")
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
        
        val emailOrPhone = binding.etEmailOrPhone.text.toString().trim()
        val password = binding.etPassword.text.toString()
        
        if (TextUtils.isEmpty(emailOrPhone)) {
            binding.tilEmailOrPhone.error = getString(R.string.error_required)
            isValid = false
        } else {
            binding.tilEmailOrPhone.error = null
        }
        
        if (TextUtils.isEmpty(password)) {
            binding.tilPassword.error = getString(R.string.error_required)
            isValid = false
        } else if (password.length < 8) {
            binding.tilPassword.error = getString(R.string.error_password_too_short)
            isValid = false
        } else {
            binding.tilPassword.error = null
        }
        
        return isValid
    }
    
    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.btnLogin.isEnabled = !show
    }
    
    private fun showError(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }
    
    private fun navigateToMain() {
        val intent = Intent(requireContext(), MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        activity?.finish()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
