package com.vaultpool.customer.presentation.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import com.vaultpool.customer.R;
import com.vaultpool.customer.ServiceLocator;
import com.vaultpool.customer.data.auth.AuthFlowManager;
import com.vaultpool.customer.data.local.prefs.PreferencesManager;
import com.vaultpool.customer.databinding.ActivityMainBinding;
import com.vaultpool.customer.presentation.ui.login.LoginActivity;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * Main Activity.
 * Entry point after successful authentication.
 * Manages Bottom Navigation and Fragment switching.
 */
public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private AuthFlowManager authFlowManager;
    private PreferencesManager preferencesManager;
    private final CompositeDisposable disposables = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authFlowManager = ServiceLocator.getInstance().getAuthFlowManager();
        preferencesManager = ServiceLocator.getInstance().getPreferencesManager();

        // Setup Navigation Component
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        
        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
            // Link BottomNavigationView with NavController
            if (binding.bottomNavigation != null) {
                NavigationUI.setupWithNavController(binding.bottomNavigation, navController);
            }
        }

        setupProfileDisplay();
        setupActions();
        refreshProfile();
    }

    private void setupProfileDisplay() {
        if (binding.tvWelcome == null) return; // Not in the simple layout mode

        boolean isStaff = preferencesManager.isStaff();
        
        // Show user dashboard button if user is not staff
        binding.btnUserDashboard.setVisibility(!isStaff ? android.view.View.VISIBLE : android.view.View.GONE);
        
        // Hide staff button if user is not staff
        binding.btnStaff.setVisibility(isStaff ? android.view.View.VISIBLE : android.view.View.GONE);
        
        bindProfile();
    }

    private void bindProfile() {
        if (binding.tvWelcome == null) return;
        
        String email = preferencesManager.getUserEmail();
        binding.tvWelcome.setText(getString(R.string.welcome_title));
        binding.tvEmailValue.setText(email != null ? email : getString(R.string.guest_user));
    }

    private void setupActions() {
        if (binding.btnLogout != null) {
            binding.btnLogout.setOnClickListener(v -> logout());
        }
        if (binding.btnUserDashboard != null) {
            binding.btnUserDashboard.setOnClickListener(v -> {
                Toast.makeText(this, "Redirecting to User Dashboard...", Toast.LENGTH_SHORT).show();
            });
        }
        if (binding.btnStaff != null) {
            binding.btnStaff.setOnClickListener(v -> {
                Intent intent = new Intent(this, com.vaultpool.customer.presentation.ui.staff.StaffActivity.class);
                startActivity(intent);
            });
        }
    }

    private void refreshProfile() {
        if (binding.tvStatusValue == null) return;

        binding.tvStatusValue.setText(getString(R.string.status_refreshing));
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
            Toast.makeText(this, getString(R.string.session_expired), Toast.LENGTH_LONG).show();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
    }

    private void logout() {
        authFlowManager.logout();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void setLoading(boolean loading) {
        if (binding.progressBar != null) {
            binding.progressBar.setVisibility(loading ? android.view.View.VISIBLE : android.view.View.GONE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disposables.clear();
        binding = null;
    }
}
