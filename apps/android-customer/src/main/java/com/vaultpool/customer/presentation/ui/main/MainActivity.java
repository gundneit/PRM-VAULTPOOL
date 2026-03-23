package com.vaultpool.customer.presentation.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.vaultpool.customer.R;
import com.vaultpool.customer.ServiceLocator;
import com.vaultpool.customer.data.auth.AuthFlowManager;
import com.vaultpool.customer.data.local.prefs.PreferencesManager;
import com.vaultpool.customer.data.remote.api.BookingApi;
import com.vaultpool.customer.databinding.ActivityMainBinding;
import com.vaultpool.customer.presentation.ui.login.LoginActivity;
import com.vaultpool.customer.presentation.ui.main.bookings.BookingsFragment;
import com.vaultpool.customer.presentation.ui.payment.ZaloPaymentActivity;

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
    private BookingApi bookingApi;
    private final CompositeDisposable disposables = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authFlowManager = ServiceLocator.getInstance().getAuthFlowManager();
        preferencesManager = ServiceLocator.getInstance().getPreferencesManager();
        bookingApi = ServiceLocator.getInstance().getBookingApi();

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
        updateCartBadge();
        handleNavigationIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleNavigationIntent(intent);
    }

    private void handleNavigationIntent(Intent intent) {
        if (intent == null)
            return;
        String navigateTo = intent.getStringExtra("navigate_to");
        if (!"bookings".equals(navigateTo))
            return;
        if (binding.bottomNavigation == null)
            return;

        String tabStatus = intent.getStringExtra("tab_status");
        if (tabStatus != null) {
            BookingsFragment.pendingTabStatus = tabStatus;
        }

        long paidBookingId = intent.getLongExtra(ZaloPaymentActivity.EXTRA_PAID_BOOKING_ID, -1L);
        if (paidBookingId > 0) {
            BookingsFragment.pendingPaidBookingId = paidBookingId;
        }

        int currentSelected = binding.bottomNavigation.getSelectedItemId();
        if (currentSelected == R.id.nav_bookings) {
            // Fragment đang active, dùng post để đảm bảo fragment đã resume
            binding.getRoot().post(() -> {
                NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.nav_host_fragment);
                if (navHostFragment != null) {
                    Fragment current = navHostFragment.getChildFragmentManager()
                            .getPrimaryNavigationFragment();
                    if (current instanceof BookingsFragment && tabStatus != null) {
                        ((BookingsFragment) current).selectTab(tabStatus);
                        BookingsFragment.pendingTabStatus = null;
                    }
                }
            });
        } else {
            // Set pending TRƯỚC khi switch tab
            binding.getRoot().post(() -> binding.bottomNavigation.setSelectedItemId(R.id.nav_bookings));
        }

        // Consume one-time navigation extras để tránh xử lý lặp
        intent.removeExtra("navigate_to");
        intent.removeExtra("tab_status");
        intent.removeExtra(ZaloPaymentActivity.EXTRA_PAID_BOOKING_ID);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateCartBadge();
    }

    private void updateCartBadge() {
        String firebaseToken = preferencesManager.getFirebaseToken();
        if (firebaseToken == null) {
            return;
        }
        String token = firebaseToken.startsWith("Bearer ") ? firebaseToken : "Bearer " + firebaseToken;

        disposables.add(
                bookingApi.getCartCount(token)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(response -> {
                            if (response != null && response.isSuccess() && response.getData() != null) {
                                int cartCount = response.getData().getCount();
                                showCartBadge(cartCount);
                            }
                        }, error -> {
                            // Silent fail - don't show error toast for badge updates
                        }));
    }

    private void showCartBadge(int count) {
        BadgeDrawable badge = binding.bottomNavigation.getOrCreateBadge(R.id.nav_tickets);
        if (count > 0) {
            badge.setVisible(true);
            badge.setNumber(count);
            badge.setMaxCharacterCount(3);
        } else {
            badge.setVisible(false);
        }
    }

    private void setupProfileDisplay() {
        if (binding.tvWelcome == null)
            return; // Not in the simple layout mode

        boolean isStaff = preferencesManager.isStaff();

        // Show user dashboard button if user is not staff
        binding.btnUserDashboard.setVisibility(!isStaff ? android.view.View.VISIBLE : android.view.View.GONE);

        // Hide staff button if user is not staff
        binding.btnStaff.setVisibility(isStaff ? android.view.View.VISIBLE : android.view.View.GONE);

        bindProfile();
    }

    private void bindProfile() {
        if (binding.tvWelcome == null)
            return;

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
        if (binding.tvStatusValue == null)
            return;

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
                        }));
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
