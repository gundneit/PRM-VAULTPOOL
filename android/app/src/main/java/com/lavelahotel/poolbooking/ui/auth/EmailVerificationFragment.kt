package com.lavelahotel.poolbooking.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.lavelahotel.poolbooking.R
import com.lavelahotel.poolbooking.databinding.FragmentEmailVerificationBinding
import com.lavelahotel.poolbooking.data.repository.FirebaseAuthRepository
import com.lavelahotel.poolbooking.ui.main.MainActivity
import kotlinx.coroutines.launch

class EmailVerificationFragment : Fragment() {
    
    private var _binding: FragmentEmailVerificationBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: FirebaseAuthViewModel by activityViewModels {
        FirebaseAuthViewModelFactory(
            FirebaseAuthRepository()
        )
    }
    
    private var email: String = ""
    
    companion object {
        private const val ARG_EMAIL = "email"
        
        fun newInstance(email: String): EmailVerificationFragment {
            return EmailVerificationFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_EMAIL, email)
                }
            }
        }
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEmailVerificationBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        email = arguments?.getString(ARG_EMAIL) ?: ""
        binding.tvEmail.text = email
        
        setupClickListeners()
        observeViewModel()
    }
    
    private fun setupClickListeners() {
        binding.btnResendEmail.setOnClickListener {
            viewModel.resendVerificationEmail()
        }
        
        binding.btnVerify.setOnClickListener {
            viewModel.checkEmailVerification()
        }
    }
    
    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is AuthUiState.Loading -> {
                        showLoading(true)
                    }
                    is AuthUiState.EmailSent -> {
                        showLoading(false)
                        Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                    }
                    is AuthUiState.EmailVerified -> {
                        showLoading(false)
                        Toast.makeText(context, "Email đã được xác thực thành công!", Toast.LENGTH_SHORT).show()
                        navigateToMain()
                    }
                    is AuthUiState.EmailNotVerified -> {
                        showLoading(false)
                        Toast.makeText(context, "Email chưa được xác thực. Vui lòng kiểm tra lại.", Toast.LENGTH_LONG).show()
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
    
    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.btnVerify.isEnabled = !show
        binding.btnResendEmail.isEnabled = !show
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
