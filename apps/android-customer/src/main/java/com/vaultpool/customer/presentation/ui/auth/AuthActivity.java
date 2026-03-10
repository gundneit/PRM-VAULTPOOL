package com.vaultpool.customer.presentation.ui.auth;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.vaultpool.customer.R;
import com.vaultpool.customer.databinding.ActivityAuthBinding;

/**
 * Main Authentication Activity.
 * Hosts Login, Register, and Email Verification fragments.
 */
public class AuthActivity extends AppCompatActivity {

    private ActivityAuthBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAuthBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (savedInstanceState == null) {
            // Show login fragment by default
            navigateToLogin();
        }
    }

    /**
     * Navigate to Login fragment
     */
    public void navigateToLogin() {
        loadFragment(new LoginFragment(), false);
    }

    /**
     * Navigate to Register fragment
     */
    public void navigateToRegister() {
        loadFragment(new RegisterFragment(), true);
    }

    /**
     * Navigate to Email Verification fragment
     */
    public void navigateToEmailVerification(String email) {
        EmailVerificationFragment fragment = EmailVerificationFragment.newInstance(email);
        loadFragment(fragment, true);
    }

    /**
     * Navigate to Phone OTP Verification fragment
     */
    public void navigateToPhoneOtpVerification() {
        loadFragment(new PhoneOtpVerificationFragment(), true);
    }

    /**
     * Load a fragment into the container
     */
    private void loadFragment(Fragment fragment, boolean addToBackStack) {
        FragmentTransaction transaction = getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(
                        R.anim.slide_in_right,
                        R.anim.slide_out_left,
                        R.anim.slide_in_left,
                        R.anim.slide_out_right
                )
                .replace(R.id.fragmentContainer, fragment);

        if (addToBackStack) {
            transaction.addToBackStack(null);
        }

        transaction.commit();
    }

    /**
     * Show/hide progress bar
     */
    public void showProgress(boolean show) {
        binding.progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
