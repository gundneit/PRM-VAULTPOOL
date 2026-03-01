package com.lavelahotel.poolbooking.ui.auth

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.lavelahotel.poolbooking.databinding.FragmentPhoneOtpVerificationBinding
import com.lavelahotel.poolbooking.ui.main.MainActivity
import java.util.concurrent.TimeUnit

class PhoneOtpVerificationFragment : Fragment() {

    private var _binding: FragmentPhoneOtpVerificationBinding? = null
    private val binding get() = _binding!!

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    private var phoneNumber: String = ""
    private var verificationId: String? = null
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null

    companion object {
        private const val ARG_PHONE = "phone"

        fun newInstance(phone: String): PhoneOtpVerificationFragment {
            return PhoneOtpVerificationFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PHONE, phone)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPhoneOtpVerificationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        phoneNumber = arguments?.getString(ARG_PHONE) ?: ""
        binding.tvPhone.text = phoneNumber

        startPhoneNumberVerification(phoneNumber)
        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnVerifyOtp.setOnClickListener {
            val code = binding.etOtp.text.toString().trim()
            if (TextUtils.isEmpty(code) || verificationId.isNullOrEmpty()) {
                Toast.makeText(context, "Vui lòng nhập mã OTP hợp lệ.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            val credential = PhoneAuthProvider.getCredential(verificationId!!, code)
            signInWithPhoneAuthCredential(credential)
        }

        binding.btnResendOtp.setOnClickListener {
            val token = resendToken
            if (token != null) {
                resendVerificationCode(phoneNumber, token)
            } else {
                Toast.makeText(context, "Không thể gửi lại mã lúc này, vui lòng thử lại sau.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun startPhoneNumberVerification(phoneNumber: String) {
        showLoading(true)
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)       // Số điện thoại phải có mã quốc gia, ví dụ: +84xxxxxxxxx
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(requireActivity())
            .setCallbacks(callbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun resendVerificationCode(
        phoneNumber: String,
        token: PhoneAuthProvider.ForceResendingToken
    ) {
        showLoading(true)
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(requireActivity())
            .setCallbacks(callbacks)
            .setForceResendingToken(token)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            // Có thể auto đọc SMS, sign in luôn
            signInWithPhoneAuthCredential(credential)
        }

        override fun onVerificationFailed(e: FirebaseException) {
            showLoading(false)
            Toast.makeText(context, "Xác thực thất bại: ${e.message}", Toast.LENGTH_LONG).show()
        }

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken
        ) {
            showLoading(false)
            this@PhoneOtpVerificationFragment.verificationId = verificationId
            this@PhoneOtpVerificationFragment.resendToken = token
            Toast.makeText(context, "Mã OTP đã được gửi.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        showLoading(true)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->
                showLoading(false)
                if (task.isSuccessful) {
                    navigateToMain()
                } else {
                    Toast.makeText(
                        context,
                        "Không thể đăng nhập bằng OTP: ${task.exception?.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    private fun navigateToMain() {
        val intent = Intent(requireContext(), MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        activity?.finish()
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.btnVerifyOtp.isEnabled = !show
        binding.btnResendOtp.isEnabled = !show
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

