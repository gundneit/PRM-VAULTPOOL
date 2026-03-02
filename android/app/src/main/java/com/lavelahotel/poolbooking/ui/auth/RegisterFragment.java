package com.lavelahotel.poolbooking.ui.auth;

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
import com.lavelahotel.poolbooking.databinding.FragmentRegisterBinding;
import com.lavelahotel.poolbooking.util.ValidationUtils;

public class RegisterFragment extends Fragment {

    private FragmentRegisterBinding binding;
    private FirebaseAuthViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentRegisterBinding.inflate(inflater, container, false);
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
        setupChannelToggle();
    }

    /**
     * Show/hide email vs phone fields based on the selected verification channel.
     */
    private void setupChannelToggle() {
        // Apply initial state based on default selection
        applyChannelVisibility(binding.rgVerificationChannel.getCheckedRadioButtonId());

        binding.rgVerificationChannel.setOnCheckedChangeListener((group, checkedId) -> {
            applyChannelVisibility(checkedId);
        });
    }

    private void applyChannelVisibility(int checkedId) {
        if (checkedId == R.id.rbSms) {
            binding.tilEmail.setVisibility(View.GONE);
            binding.tilPassword.setVisibility(View.GONE);
            binding.tilConfirmPassword.setVisibility(View.GONE);
            binding.tilPhone.setVisibility(View.VISIBLE);
            binding.tilEmail.setError(null);
            binding.tilPassword.setError(null);
            binding.tilConfirmPassword.setError(null);
        } else {
            // Email channel (or nothing selected yet)
            binding.tilEmail.setVisibility(View.VISIBLE);
            binding.tilPassword.setVisibility(View.VISIBLE);
            binding.tilConfirmPassword.setVisibility(View.VISIBLE);
            binding.tilPhone.setVisibility(View.GONE);
            binding.tilPhone.setError(null);
        }
    }

    private void setupClickListeners() {
        binding.btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateInput()) {
                    int selectedChannelId = binding.rgVerificationChannel.getCheckedRadioButtonId();

                    if (selectedChannelId == R.id.rbEmail) {
                        String email = binding.etEmail.getText().toString().trim();
                        String fullName = binding.etFullName.getText().toString().trim();
                        String password = binding.etPassword.getText().toString();
                        viewModel.register(email, password, fullName);
                    } else if (selectedChannelId == R.id.rbSms) {
                        String rawPhone = binding.etPhone.getText().toString().trim();
                        // Convert to E.164 format required by Firebase Phone Auth
                        String phone = ValidationUtils.formatToE164Vietnam(rawPhone);
                        if (getActivity() instanceof AuthActivity) {
                            ((AuthActivity) getActivity()).navigateToPhoneOtp(phone);
                        }
                    } else {
                        Toast.makeText(
                                getContext(),
                                "Vui lòng chọn phương thức nhận mã (Email hoặc SMS).",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
            }
        });

        binding.tvLoginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() instanceof AuthActivity) {
                    ((AuthActivity) getActivity()).navigateToLogin();
                }
            }
        });
    }

    private void observeViewModel() {
        viewModel.getUiState().observe(getViewLifecycleOwner(), new Observer<AuthUiState>() {
            @Override
            public void onChanged(AuthUiState state) {
                if (state instanceof AuthUiState.Loading) {
                    showLoading(true);
                } else if (state instanceof AuthUiState.RegisterSuccess) {
                    showLoading(false);
                    AuthUiState.RegisterSuccess s = (AuthUiState.RegisterSuccess) state;
                    Toast.makeText(getContext(), s.message, Toast.LENGTH_LONG).show();
                    if (getActivity() instanceof AuthActivity) {
                        ((AuthActivity) getActivity()).navigateToLogin();
                    }
                    viewModel.resetState();
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
        int selectedChannelId = binding.rgVerificationChannel.getCheckedRadioButtonId();

        // Full name is required regardless of channel
        String fullName = binding.etFullName.getText().toString().trim();
        if (TextUtils.isEmpty(fullName)) {
            binding.tilFullName.setError(getString(R.string.error_required));
            isValid = false;
        } else {
            binding.tilFullName.setError(null);
        }

        if (selectedChannelId == -1) {
            Toast.makeText(
                    getContext(),
                    "Vui lòng chọn phương thức nhận mã (Email hoặc SMS).",
                    Toast.LENGTH_LONG
            ).show();
            isValid = false;
        } else if (selectedChannelId == R.id.rbEmail) {
            // --- Email channel: validate email + password ---
            String email = binding.etEmail.getText().toString().trim();
            if (TextUtils.isEmpty(email)) {
                binding.tilEmail.setError(getString(R.string.error_required));
                isValid = false;
            } else if (!ValidationUtils.isValidEmail(email)) {
                binding.tilEmail.setError(getString(R.string.error_invalid_email));
                isValid = false;
            } else {
                binding.tilEmail.setError(null);
            }

            String password = binding.etPassword.getText().toString();
            String confirmPassword = binding.etConfirmPassword.getText().toString();

            if (TextUtils.isEmpty(password)) {
                binding.tilPassword.setError(getString(R.string.error_required));
                isValid = false;
            } else if (password.length() < 6) {
                binding.tilPassword.setError("Mật khẩu phải có ít nhất 6 ký tự");
                isValid = false;
            } else {
                binding.tilPassword.setError(null);
            }

            if (TextUtils.isEmpty(confirmPassword)) {
                binding.tilConfirmPassword.setError(getString(R.string.error_required));
                isValid = false;
            } else if (!password.equals(confirmPassword)) {
                binding.tilConfirmPassword.setError(getString(R.string.error_password_mismatch));
                isValid = false;
            } else {
                binding.tilConfirmPassword.setError(null);
            }

        } else if (selectedChannelId == R.id.rbSms) {
            // --- SMS channel: validate phone number only ---
            String phone = binding.etPhone.getText().toString().trim();
            if (TextUtils.isEmpty(phone)) {
                binding.tilPhone.setError(getString(R.string.error_required));
                isValid = false;
            } else if (!ValidationUtils.isValidPhone(phone)) {
                binding.tilPhone.setError(getString(R.string.error_invalid_phone));
                isValid = false;
            } else {
                binding.tilPhone.setError(null);
            }
        }

        return isValid;
    }

    private void showLoading(boolean show) {
        binding.progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        binding.btnRegister.setEnabled(!show);
    }

    private void showError(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

