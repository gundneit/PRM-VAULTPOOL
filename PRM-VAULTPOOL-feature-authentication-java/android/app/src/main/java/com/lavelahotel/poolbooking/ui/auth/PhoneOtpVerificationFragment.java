package com.lavelahotel.poolbooking.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.lavelahotel.poolbooking.databinding.FragmentPhoneOtpVerificationBinding;
import com.lavelahotel.poolbooking.ui.main.MainActivity;

import java.util.concurrent.TimeUnit;

public class PhoneOtpVerificationFragment extends Fragment {

    private static final String ARG_PHONE = "phone";

    private FragmentPhoneOtpVerificationBinding binding;
    private FirebaseAuth auth;

    private String phoneNumber = "";
    private String verificationId;
    private PhoneAuthProvider.ForceResendingToken resendToken;

    public static PhoneOtpVerificationFragment newInstance(String phone) {
        PhoneOtpVerificationFragment fragment = new PhoneOtpVerificationFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PHONE, phone);
        fragment.setArguments(args);
        return fragment;
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

        auth = FirebaseAuth.getInstance();

        if (getArguments() != null) {
            phoneNumber = getArguments().getString(ARG_PHONE, "");
        }
        binding.tvPhone.setText(phoneNumber);

        startPhoneNumberVerification(phoneNumber);
        setupClickListeners();
    }

    private void setupClickListeners() {
        binding.btnVerifyOtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String code = binding.etOtp.getText().toString().trim();
                if (TextUtils.isEmpty(code) || TextUtils.isEmpty(verificationId)) {
                    Toast.makeText(getContext(),
                            "Vui lòng nhập mã OTP hợp lệ.",
                            Toast.LENGTH_LONG).show();
                    return;
                }
                PhoneAuthCredential credential =
                        PhoneAuthProvider.getCredential(verificationId, code);
                signInWithPhoneAuthCredential(credential);
            }
        });

        binding.btnResendOtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (resendToken != null) {
                    resendVerificationCode(phoneNumber, resendToken);
                } else {
                    Toast.makeText(getContext(),
                            "Không thể gửi lại mã lúc này, vui lòng thử lại sau.",
                            Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void startPhoneNumberVerification(String phoneNumber) {
        showLoading(true);
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(auth)
                        .setPhoneNumber(phoneNumber)
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setActivity(requireActivity())
                        .setCallbacks(callbacks)
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void resendVerificationCode(String phoneNumber,
                                        PhoneAuthProvider.ForceResendingToken token) {
        showLoading(true);
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(auth)
                        .setPhoneNumber(phoneNumber)
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setActivity(requireActivity())
                        .setCallbacks(callbacks)
                        .setForceResendingToken(token)
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private final PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks =
            new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                @Override
                public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                    signInWithPhoneAuthCredential(credential);
                }

                @Override
                public void onVerificationFailed(@NonNull FirebaseException e) {
                    showLoading(false);
                    Toast.makeText(getContext(),
                            "Xác thực thất bại: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                }

                @Override
                public void onCodeSent(@NonNull String verificationId,
                                       @NonNull PhoneAuthProvider.ForceResendingToken token) {
                    showLoading(false);
                    PhoneOtpVerificationFragment.this.verificationId = verificationId;
                    PhoneOtpVerificationFragment.this.resendToken = token;
                    Toast.makeText(getContext(),
                            "Mã OTP đã được gửi.",
                            Toast.LENGTH_SHORT).show();
                }
            };

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        showLoading(true);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(requireActivity(), task -> {
                    showLoading(false);
                    if (task.isSuccessful()) {
                        navigateToMain();
                    } else {
                        Toast.makeText(
                                getContext(),
                                "Không thể đăng nhập bằng OTP: " +
                                        (task.getException() != null
                                                ? task.getException().getMessage()
                                                : ""),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }

    private void navigateToMain() {
        Intent intent = new Intent(requireContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        if (getActivity() != null) {
            getActivity().finish();
        }
    }

    private void showLoading(boolean show) {
        binding.progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        binding.btnVerifyOtp.setEnabled(!show);
        binding.btnResendOtp.setEnabled(!show);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

