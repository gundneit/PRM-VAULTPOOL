package com.vaultpool.customer.presentation.ui.splash;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import com.vaultpool.customer.R;
import com.vaultpool.customer.ServiceLocator;
import com.vaultpool.customer.data.local.prefs.PreferencesManager;
import com.vaultpool.customer.databinding.ActivitySplashBinding;
import com.vaultpool.customer.presentation.ui.main.MainActivity;
import com.vaultpool.customer.presentation.ui.onboarding.OnboardingActivity;

public class SplashActivity extends AppCompatActivity {

    private ActivitySplashBinding binding;
    private PreferencesManager preferencesManager;
    private static final int SPLASH_DELAY = 1500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 1. Khởi tạo SplashScreen API nhưng không giữ nó lại lâu
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
        
        super.onCreate(savedInstanceState);
        
        // 2. Sử dụng layout có Logo hình vuông
        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 3. Đảm bảo Logo hình vuông hiển thị (không bị hệ thống che)
        binding.ivSplashLogo.setImageResource(R.drawable.logo);
        binding.ivSplashLogo.setVisibility(View.VISIBLE);
        binding.tvSplashLogo.setVisibility(View.GONE);

        ServiceLocator.getInstance().init(getApplicationContext());
        preferencesManager = ServiceLocator.getInstance().getPreferencesManager();

        navigateToNextScreen();
    }

    private void navigateToNextScreen() {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (isFinishing()) return;
            
            Intent intent = new Intent(
                    this,
                    hasActiveSession() ? MainActivity.class : OnboardingActivity.class
            );
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }, SPLASH_DELAY);
    }

    private boolean hasActiveSession() {
        return preferencesManager != null
                && preferencesManager.isLoggedIn()
                && preferencesManager.getFirebaseToken() != null;
    }
}
