package com.lavelahotel.poolbooking.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.lavelahotel.poolbooking.databinding.ActivityMainBinding;
import com.lavelahotel.poolbooking.ui.auth.AuthActivity;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            binding = ActivityMainBinding.inflate(getLayoutInflater());
            setContentView(binding.getRoot());

            binding.tvWelcome.setText("Chào mừng đến với La Vela Pool Booking!");

            binding.btnLogout.setOnClickListener(v -> {
                try {
                    FirebaseAuth.getInstance().signOut();
                    Intent intent = new Intent(this, AuthActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } catch (Exception e) {
                    Log.e(TAG, "Error while logging out", e);
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate", e);
            throw e;
        }
    }
}

