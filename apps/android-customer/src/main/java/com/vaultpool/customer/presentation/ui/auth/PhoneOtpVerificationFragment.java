package com.vaultpool.customer.presentation.ui.auth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.vaultpool.customer.databinding.FragmentPhoneOtpVerificationBinding;

/**
 * Phone OTP Verification Fragment.
 * Handles phone number verification flow.
 */
public class PhoneOtpVerificationFragment extends Fragment {

    private static final String ARG_PHONE = "phone";
    private FragmentPhoneOtpVerificationBinding binding;
    private String phone;

    public static PhoneOtpVerificationFragment newInstance(String phone) {
        PhoneOtpVerificationFragment fragment = new PhoneOtpVerificationFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PHONE, phone);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            phone = getArguments().getString(ARG_PHONE);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentPhoneOtpVerificationBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (phone != null && !phone.isEmpty()) {
            binding.tvPhone.setText(phone);
        }

        binding.btnVerifyOtp.setOnClickListener(v ->
            Toast.makeText(getContext(), "OTP verify", Toast.LENGTH_SHORT).show());

        binding.btnResendOtp.setOnClickListener(v ->
            Toast.makeText(getContext(), "Resend OTP", Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
