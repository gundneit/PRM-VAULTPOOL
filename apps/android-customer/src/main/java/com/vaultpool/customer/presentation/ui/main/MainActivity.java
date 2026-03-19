package com.vaultpool.customer.presentation.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

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
 */
public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private final CompositeDisposable disposables = new CompositeDisposable();
    private PreferencesManager preferencesManager;
    private AuthFlowManager authFlowManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ServiceLocator.getInstance().init(getApplicationContext());
        preferencesManager = ServiceLocator.getInstance().getPreferencesManager();
        authFlowManager = ServiceLocator.getInstance().getAuthFlowManager();

        bindProfile();
        setupActions();
        refreshProfile();
    }

    private void bindProfile() {
        String userName = preferencesManager.getUserName();
        String userEmail = preferencesManager.getUserEmail();
        String userId = preferencesManager.getUserId();
        boolean loggedIn = preferencesManager.isLoggedIn();
        boolean isStaff = preferencesManager.isStaff();

        binding.tvWelcome.setText(userName != null && !userName.isEmpty()
                ? userName
                : getString(com.vaultpool.customer.R.string.guest_user));
        binding.tvEmailValue.setText(userEmail != null && !userEmail.isEmpty() ? userEmail : "-");
        binding.tvUserIdValue.setText(userId != null && !userId.isEmpty() ? userId : "-");
        binding.tvStatusValue.setText(getString(
                loggedIn
                        ? com.vaultpool.customer.R.string.status_active
                        : com.vaultpool.customer.R.string.status_missing
        ));
        
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
        }

        if (message != null && !message.isEmpty()) {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        }
    }

    private void logout() {
        setLoading(true);
        disposables.add(
                authFlowManager.logout()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(result -> {
                            setLoading(false);
                            Intent intent = new Intent(this, LoginActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        }, throwable -> {
                            setLoading(false);
                            Toast.makeText(this, throwable.getMessage(), Toast.LENGTH_LONG).show();
                        })
        );
    }

    private void setLoading(boolean loading) {
        binding.progressBar.setVisibility(loading ? android.view.View.VISIBLE : android.view.View.GONE);
        binding.btnLogout.setEnabled(!loading);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disposables.clear();
        binding = null;
    }
}
