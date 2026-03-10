package com.vaultpool.customer.presentation.ui.splash;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.vaultpool.customer.ServiceLocator;
import com.vaultpool.customer.databinding.ActivitySplashBinding;
import com.vaultpool.customer.domain.repository.AuthRepository;
import com.vaultpool.customer.presentation.ui.auth.AuthActivity;
import com.vaultpool.customer.presentation.ui.main.MainActivity;

/**
 * Splash Activity.
 * Handles app startup and navigation based on auth state.
 */
public class SplashActivity extends AppCompatActivity {

    private ActivitySplashBinding binding;
    private AuthRepository authRepository;

    private static final int SPLASH_DELAY = 2000; // 2 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize ServiceLocator
        ServiceLocator.getInstance().init(getApplicationContext());
        authRepository = ServiceLocator.getInstance().getAuthRepository();

        navigateToNextScreen();
    }

    private void navigateToNextScreen() {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent;

            if (authRepository.isLoggedIn()) {
                // User is logged in, go to main
                intent = new Intent(this, MainActivity.class);
            } else {
                // User is not logged in, go to auth
                intent = new Intent(this, AuthActivity.class);
            }

            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }, SPLASH_DELAY);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
