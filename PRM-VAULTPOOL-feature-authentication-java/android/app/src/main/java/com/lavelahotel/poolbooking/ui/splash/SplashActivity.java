package com.lavelahotel.poolbooking.ui.splash;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.lavelahotel.poolbooking.R;
import com.lavelahotel.poolbooking.ui.auth.AuthActivity;
import com.lavelahotel.poolbooking.ui.main.MainActivity;

public class SplashActivity extends AppCompatActivity {

    private static final String TAG = "SplashActivity";
    private static final long SPLASH_DELAY = 2000L;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreate started");

        try {
            super.onCreate(savedInstanceState);
            Log.d(TAG, "super.onCreate() completed");
        } catch (Exception e) {
            Log.e(TAG, "Error in super.onCreate()", e);
            throw e;
        }

        try {
            Log.d(TAG, "Setting content view...");
            setContentView(R.layout.activity_splash);
            Log.d(TAG, "Content view set successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error setting content view", e);
            throw e;
        }

        try {
            Log.d(TAG, "Starting delayed navigation...");
            new Handler(Looper.getMainLooper()).postDelayed(
                    this::navigateToNextScreen,
                    SPLASH_DELAY
            );
            Log.d(TAG, "onCreate completed successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error starting delay", e);
            navigateToAuth();
        }
    }

    private void navigateToNextScreen() {
        try {
            FirebaseAuth auth = FirebaseAuth.getInstance();
            FirebaseUser currentUser = auth.getCurrentUser();

            Intent intent;
            if (currentUser != null && currentUser.isEmailVerified()) {
                Log.d(TAG, "User logged in, navigating to MainActivity");
                intent = new Intent(this, MainActivity.class);
            } else {
                Log.d(TAG, "User not logged in, navigating to AuthActivity");
                intent = new Intent(this, AuthActivity.class);
            }

            startActivity(intent);
            finish();
        } catch (Exception e) {
            Log.e(TAG, "Error navigating to next screen", e);
            navigateToAuth();
        }
    }

    private void navigateToAuth() {
        try {
            Intent intent = new Intent(this, AuthActivity.class);
            startActivity(intent);
            finish();
        } catch (Exception e) {
            Log.e(TAG, "Critical error: Cannot navigate to AuthActivity", e);
            finish();
        }
    }
}

