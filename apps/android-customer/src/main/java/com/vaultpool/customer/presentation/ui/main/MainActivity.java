package com.vaultpool.customer.presentation.ui.main;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.vaultpool.customer.databinding.ActivityMainBinding;

/**
 * Main Activity.
 * Entry point after successful authentication.
 */
public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // TODO: Setup main app content
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
