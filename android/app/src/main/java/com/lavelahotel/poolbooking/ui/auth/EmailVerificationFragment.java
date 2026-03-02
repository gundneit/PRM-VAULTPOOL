package com.lavelahotel.poolbooking.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.lavelahotel.poolbooking.data.repository.FirebaseAuthRepository;
import com.lavelahotel.poolbooking.databinding.FragmentEmailVerificationBinding;
import com.lavelahotel.poolbooking.ui.main.MainActivity;

public class EmailVerificationFragment extends Fragment {

    private static final String ARG_EMAIL = "email";

    private FragmentEmailVerificationBinding binding;
    private FirebaseAuthViewModel viewModel;
    private String email = "";

    public static EmailVerificationFragment newInstance(String email) {
        EmailVerificationFragment fragment = new EmailVerificationFragment();
        Bundle args = new Bundle();
        args.putString(ARG_EMAIL, email);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentEmailVerificationBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            email = getArguments().getString(ARG_EMAIL, "");
        }
        binding.tvEmail.setText(email);

        ViewModelProvider.Factory factory =
                new FirebaseAuthViewModelFactory(new FirebaseAuthRepository());
        viewModel = new ViewModelProvider(requireActivity(), factory)
                .get(FirebaseAuthViewModel.class);

        setupClickListeners();
        observeViewModel();
    }

    private void setupClickListeners() {
        binding.btnResendEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewModel.resendVerificationEmail();
            }
        });

        binding.btnVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewModel.checkEmailVerification();
            }
        });
    }

    private void observeViewModel() {
        viewModel.getUiState().observe(getViewLifecycleOwner(), new Observer<AuthUiState>() {
            @Override
            public void onChanged(AuthUiState state) {
                if (state instanceof AuthUiState.Loading) {
                    showLoading(true);
                } else if (state instanceof AuthUiState.EmailSent) {
                    showLoading(false);
                    AuthUiState.EmailSent s = (AuthUiState.EmailSent) state;
                    Toast.makeText(getContext(), s.message, Toast.LENGTH_LONG).show();
                } else if (state instanceof AuthUiState.EmailVerified) {
                    showLoading(false);
                    Toast.makeText(getContext(),
                            "Email đã được xác thực thành công!",
                            Toast.LENGTH_SHORT).show();
                    navigateToMain();
                } else if (state instanceof AuthUiState.EmailNotVerified) {
                    showLoading(false);
                    Toast.makeText(getContext(),
                            "Email chưa được xác thực. Vui lòng kiểm tra lại.",
                            Toast.LENGTH_LONG).show();
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

    private void showLoading(boolean show) {
        binding.progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        binding.btnVerify.setEnabled(!show);
        binding.btnResendEmail.setEnabled(!show);
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

