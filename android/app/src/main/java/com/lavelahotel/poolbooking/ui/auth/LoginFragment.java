package com.lavelahotel.poolbooking.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.lavelahotel.poolbooking.R;
import com.lavelahotel.poolbooking.data.repository.FirebaseAuthRepository;
import com.lavelahotel.poolbooking.databinding.FragmentLoginBinding;
import com.lavelahotel.poolbooking.ui.main.MainActivity;

public class LoginFragment extends Fragment {

    private FragmentLoginBinding binding;
    private FirebaseAuthViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ViewModelProvider.Factory factory =
                new FirebaseAuthViewModelFactory(new FirebaseAuthRepository());
        viewModel = new ViewModelProvider(requireActivity(), factory)
                .get(FirebaseAuthViewModel.class);

        setupClickListeners();
        observeViewModel();
    }

    private void setupClickListeners() {
        binding.btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateInput()) {
                    String emailOrPhone = binding.etEmailOrPhone.getText().toString().trim();
                    String password = binding.etPassword.getText().toString();
                    viewModel.login(emailOrPhone, password);
                }
            }
        });

        binding.tvRegisterLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() instanceof AuthActivity) {
                    ((AuthActivity) getActivity()).navigateToRegister();
                }
            }
        });

        binding.tvForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "Tính năng đang phát triển", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void observeViewModel() {
        viewModel.getUiState().observe(getViewLifecycleOwner(), new Observer<AuthUiState>() {
            @Override
            public void onChanged(AuthUiState state) {
                if (state instanceof AuthUiState.Loading) {
                    showLoading(true);
                } else if (state instanceof AuthUiState.LoginSuccess) {
                    showLoading(false);
                    Toast.makeText(getContext(), getString(R.string.login_success), Toast.LENGTH_SHORT).show();
                    navigateToMain();
                } else if (state instanceof AuthUiState.EmailNotVerified) {
                    showLoading(false);
                    AuthUiState.EmailNotVerified s = (AuthUiState.EmailNotVerified) state;
                    Toast.makeText(getContext(), s.message, Toast.LENGTH_LONG).show();
                    if (getActivity() instanceof AuthActivity) {
                        String email = s.user.getEmail() != null ? s.user.getEmail() : "";
                        ((AuthActivity) getActivity()).navigateToEmailVerification(email);
                    }
                } else if (state instanceof AuthUiState.Error) {
                    showLoading(false);
                    AuthUiState.Error s = (AuthUiState.Error) state;
                    showError(s.message);
                } else {
                    showLoading(false);
                }
            }
        });
    }

    private boolean validateInput() {
        boolean isValid = true;

        String emailOrPhone = binding.etEmailOrPhone.getText().toString().trim();
        String password = binding.etPassword.getText().toString();

        if (TextUtils.isEmpty(emailOrPhone)) {
            binding.tilEmailOrPhone.setError(getString(R.string.error_required));
            isValid = false;
        } else {
            binding.tilEmailOrPhone.setError(null);
        }

        if (TextUtils.isEmpty(password)) {
            binding.tilPassword.setError(getString(R.string.error_required));
            isValid = false;
        } else if (password.length() < 8) {
            binding.tilPassword.setError(getString(R.string.error_password_too_short));
            isValid = false;
        } else {
            binding.tilPassword.setError(null);
        }

        return isValid;
    }

    private void showLoading(boolean show) {
        binding.progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        binding.btnLogin.setEnabled(!show);
    }

    private void showError(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
    }

    private void navigateToMain() {
        Intent intent = new Intent(requireContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        if (getActivity() != null) {
            getActivity().finish();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

