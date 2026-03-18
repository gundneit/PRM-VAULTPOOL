package com.vaultpool.customer.presentation.ui.staff;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.vaultpool.customer.databinding.ActivityStaffBinding;

public class StaffActivity extends AppCompatActivity {

    private ActivityStaffBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStaffBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupToolbar();
        setupNavigation();
        
        // Default fragment
        if (savedInstanceState == null) {
            loadFragment(new StaffBookingsFragment());
        }
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        binding.btnBack.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
    }

    private void setupNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == com.vaultpool.customer.R.id.navigation_bookings) {
                loadFragment(new StaffBookingsFragment());
                return true;
            } else if (itemId == com.vaultpool.customer.R.id.navigation_pools) {
                loadFragment(new StaffPoolsFragment());
                return true;
            }
            return false;
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(com.vaultpool.customer.R.id.nav_host_fragment, fragment)
                .commit();
    }
}
