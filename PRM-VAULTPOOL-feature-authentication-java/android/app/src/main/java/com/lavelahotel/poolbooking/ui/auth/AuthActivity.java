package com.lavelahotel.poolbooking.ui.auth;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.lavelahotel.poolbooking.R;
import com.lavelahotel.poolbooking.databinding.ActivityAuthBinding;

public class AuthActivity extends AppCompatActivity {

    private static final String TAG = "AuthActivity";

    private ActivityAuthBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            binding = ActivityAuthBinding.inflate(getLayoutInflater());
            setContentView(binding.getRoot());

            if (savedInstanceState == null) {
                FragmentTransaction tx = getSupportFragmentManager().beginTransaction();
                tx.replace(R.id.fragmentContainer, new LoginFragment());
                tx.commit();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate", e);
            throw e;
        }
    }

    public void navigateToRegister() {
        FragmentTransaction tx = getSupportFragmentManager().beginTransaction();
        tx.replace(R.id.fragmentContainer, new RegisterFragment());
        tx.addToBackStack(null);
        tx.commit();
    }

    public void navigateToLogin() {
        FragmentTransaction tx = getSupportFragmentManager().beginTransaction();
        tx.replace(R.id.fragmentContainer, new LoginFragment());
        tx.addToBackStack(null);
        tx.commit();
    }

    public void navigateToEmailVerification(String email) {
        EmailVerificationFragment fragment = EmailVerificationFragment.newInstance(email);
        FragmentTransaction tx = getSupportFragmentManager().beginTransaction();
        tx.replace(R.id.fragmentContainer, fragment);
        tx.addToBackStack(null);
        tx.commit();
    }

    public void navigateToPhoneOtp(String phone) {
        PhoneOtpVerificationFragment fragment = PhoneOtpVerificationFragment.newInstance(phone);
        FragmentTransaction tx = getSupportFragmentManager().beginTransaction();
        tx.replace(R.id.fragmentContainer, fragment);
        tx.addToBackStack(null);
        tx.commit();
    }
}

