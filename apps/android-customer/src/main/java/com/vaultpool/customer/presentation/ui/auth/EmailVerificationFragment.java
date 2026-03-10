package com.vaultpool.customer.presentation.ui.auth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.vaultpool.customer.databinding.FragmentEmailVerificationBinding;

/**
 * Email Verification Fragment.
 * Handles email verification flow.
 */
public class EmailVerificationFragment extends Fragment {

    private static final String ARG_EMAIL = "email";
    private FragmentEmailVerificationBinding binding;
    private String email;

    public static EmailVerificationFragment newInstance(String email) {
        EmailVerificationFragment fragment = new EmailVerificationFragment();
        Bundle args = new Bundle();
        args.putString(ARG_EMAIL, email);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            email = getArguments().getString(ARG_EMAIL);
        }
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

        if (email != null && !email.isEmpty()) {
            binding.tvEmail.setText(email);
        }

        binding.btnResendEmail.setOnClickListener(v ->
            Toast.makeText(getContext(), "Tính năng đang phát triển", Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
