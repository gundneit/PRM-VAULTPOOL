package com.vaultpool.customer.presentation.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.vaultpool.customer.ServiceLocator;
import com.vaultpool.customer.data.auth.AuthFlowManager;
import com.vaultpool.customer.databinding.ActivityLoginBinding;
import com.vaultpool.customer.presentation.ui.main.MainActivity;
import com.vaultpool.customer.presentation.ui.register.RegisterActivity;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private final CompositeDisposable disposables = new CompositeDisposable();
    private AuthFlowManager authFlowManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ServiceLocator.getInstance().init(getApplicationContext());
        authFlowManager = ServiceLocator.getInstance().getAuthFlowManager();

        setupViews();
    }

    private void setupViews() {
        binding.btnLogin.setOnClickListener(v -> {
            String email = binding.etEmail.getText() != null ? binding.etEmail.getText().toString().trim() : "";
            String password = binding.etPassword.getText() != null ? binding.etPassword.getText().toString().trim() : "";

            binding.tilEmail.setError(email.isEmpty() ? "Email is required" : null);
            binding.tilPassword.setError(password.isEmpty() ? "Password is required" : null);

            if (!email.isEmpty() && !password.isEmpty()) {
                login(email, password);
            }
        });

        binding.tvForgotPassword.setOnClickListener(v -> sendPasswordReset());

        binding.btnGoogle.setOnClickListener(v ->
                Toast.makeText(this, "Google sign-in coming soon", Toast.LENGTH_SHORT).show());

        binding.btnApple.setOnClickListener(v ->
                Toast.makeText(this, "Apple sign-in coming soon", Toast.LENGTH_SHORT).show());

        binding.tvSignUp.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));
    }

    private void login(String email, String password) {
        setLoading(true);
        disposables.add(
                authFlowManager.login(email, password)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(result -> {
                            setLoading(false);
                            if (result.isSuccess()) {
                                Intent intent = new Intent(this, MainActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
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

    private void sendPasswordReset() {
        String email = binding.etEmail.getText() != null ? binding.etEmail.getText().toString().trim() : "";
        binding.tilEmail.setError(email.isEmpty() ? "Email is required" : null);
        if (email.isEmpty()) {
            return;
        }

        setLoading(true);
        disposables.add(
                authFlowManager.sendPasswordReset(email)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(result -> {
                            setLoading(false);
                            if (result.isSuccess()) {
                                Toast.makeText(this, getString(com.vaultpool.customer.R.string.reset_password_sent), Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(this, result.getErrorMessage(), Toast.LENGTH_LONG).show();
                            }
                        }, throwable -> {
                            setLoading(false);
                            Toast.makeText(this, throwable.getMessage(), Toast.LENGTH_LONG).show();
                        })
        );
    }

    private void setLoading(boolean loading) {
        binding.progressBar.setVisibility(loading ? android.view.View.VISIBLE : android.view.View.GONE);
        binding.btnLogin.setEnabled(!loading);
        binding.tvForgotPassword.setEnabled(!loading);
        binding.tvSignUp.setEnabled(!loading);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disposables.clear();
        binding = null;
    }
}