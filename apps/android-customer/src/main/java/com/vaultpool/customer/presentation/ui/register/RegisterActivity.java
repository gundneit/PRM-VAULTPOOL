package com.vaultpool.customer.presentation.ui.register;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.vaultpool.customer.ServiceLocator;
import com.vaultpool.customer.data.auth.AuthFlowManager;
import com.vaultpool.customer.databinding.FragmentRegisterBinding;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class RegisterActivity extends AppCompatActivity {

    private FragmentRegisterBinding binding;
    private final CompositeDisposable disposables = new CompositeDisposable();
    private AuthFlowManager authFlowManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = FragmentRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ServiceLocator.getInstance().init(getApplicationContext());
        authFlowManager = ServiceLocator.getInstance().getAuthFlowManager();

        setupViews();
    }

    private void setupViews() {
        binding.rbEmail.setChecked(true);
        binding.rgVerificationChannel.setEnabled(false);
        binding.rbSms.setEnabled(false);

        binding.btnRegister.setOnClickListener(v -> attemptRegister());
        binding.tvLoginLink.setOnClickListener(v -> finish());
    }

    private void attemptRegister() {
        String fullName = binding.etFullName.getText() != null ? binding.etFullName.getText().toString().trim() : "";
        String email = binding.etEmail.getText() != null ? binding.etEmail.getText().toString().trim() : "";
        String phone = binding.etPhone.getText() != null ? binding.etPhone.getText().toString().trim() : "";
        String password = binding.etPassword.getText() != null ? binding.etPassword.getText().toString().trim() : "";
        String confirmPassword = binding.etConfirmPassword.getText() != null ? binding.etConfirmPassword.getText().toString().trim() : "";

        clearErrors();

        boolean valid = true;
        if (TextUtils.isEmpty(fullName)) {
            binding.tilFullName.setError("Full name is required");
            valid = false;
        }
        if (TextUtils.isEmpty(email)) {
            binding.tilEmail.setError("Email is required");
            valid = false;
        }
        if (TextUtils.isEmpty(password)) {
            binding.tilPassword.setError("Password is required");
            valid = false;
        }
        if (password.length() < 6) {
            binding.tilPassword.setError("Password must be at least 6 characters");
            valid = false;
        }
        if (!password.equals(confirmPassword)) {
            binding.tilConfirmPassword.setError("Passwords do not match");
            valid = false;
        }

        if (!valid) {
            return;
        }

        setLoading(true);
        disposables.add(
                authFlowManager.register(email, password, fullName, phone)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(result -> {
                            setLoading(false);
                            if (result.isSuccess()) {
                                Toast.makeText(this, "Account created. Please verify your email before logging in.", Toast.LENGTH_LONG).show();
                                finish();
                            } else {
                                Toast.makeText(this, result.getErrorMessage(), Toast.LENGTH_LONG).show();
                            }
                        }, throwable -> {
                            setLoading(false);
                            Toast.makeText(this, throwable.getMessage(), Toast.LENGTH_LONG).show();
                        })
        );
    }

    private void clearErrors() {
        binding.tilFullName.setError(null);
        binding.tilEmail.setError(null);
        binding.tilPhone.setError(null);
        binding.tilPassword.setError(null);
        binding.tilConfirmPassword.setError(null);
    }

    private void setLoading(boolean loading) {
        binding.progressBar.setVisibility(loading ? android.view.View.VISIBLE : android.view.View.GONE);
        binding.btnRegister.setEnabled(!loading);
        binding.tvLoginLink.setEnabled(!loading);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disposables.clear();
        binding = null;
    }
}