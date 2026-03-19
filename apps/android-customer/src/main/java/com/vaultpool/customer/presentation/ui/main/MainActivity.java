package com.vaultpool.customer.presentation.ui.main;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import com.vaultpool.customer.R;
import com.vaultpool.customer.databinding.ActivityMainBinding;

/**
 * Main Activity.
 * Entry point after successful authentication.
 * Manages Bottom Navigation and Fragment switching.
 */
public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Setup Navigation Component
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        
<<<<<<< HEAD
        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
            // Link BottomNavigationView with NavController
            NavigationUI.setupWithNavController(binding.bottomNavigation, navController);
=======
        // Show user dashboard button if user is not staff
        binding.btnUserDashboard.setVisibility(!isStaff ? android.view.View.VISIBLE : android.view.View.GONE);
        
        // Hide staff button if user is not staff
        binding.btnStaff.setVisibility(isStaff ? android.view.View.VISIBLE : android.view.View.GONE);
    }

    private void setupActions() {
        binding.btnLogout.setOnClickListener(v -> logout());
        binding.btnUserDashboard.setOnClickListener(v -> {
            Toast.makeText(this, "Redirecting to User Dashboard...", Toast.LENGTH_SHORT).show();
        });
        binding.btnStaff.setOnClickListener(v -> {
            Intent intent = new Intent(this, com.vaultpool.customer.presentation.ui.staff.StaffActivity.class);
            startActivity(intent);
        });
    }

    private void refreshProfile() {
        binding.tvStatusValue.setText(getString(com.vaultpool.customer.R.string.status_refreshing));
        setLoading(true);

        disposables.add(
                authFlowManager.refreshProfile()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(result -> {
                            setLoading(false);
                            if (result.isSuccess()) {
                                bindProfile();
                            } else {
                                handleRefreshFailure(result.getErrorMessage());
                            }
                        }, throwable -> {
                            setLoading(false);
                            handleRefreshFailure(throwable.getMessage());
                        })
        );
    }

    private void handleRefreshFailure(String message) {
        bindProfile();
        if (!preferencesManager.isLoggedIn()) {
            Toast.makeText(this, getString(com.vaultpool.customer.R.string.session_expired), Toast.LENGTH_LONG).show();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
>>>>>>> ceb226ad88f1dd6e98540f47a1007b48e91ae017
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
