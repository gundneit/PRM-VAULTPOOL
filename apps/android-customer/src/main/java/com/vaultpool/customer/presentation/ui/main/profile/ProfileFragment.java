package com.vaultpool.customer.presentation.ui.main.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.vaultpool.customer.ServiceLocator;
import com.vaultpool.customer.data.auth.AuthFlowManager;
import com.vaultpool.customer.data.local.prefs.PreferencesManager;
import com.vaultpool.customer.databinding.FragmentProfileBinding;
import com.vaultpool.customer.presentation.ui.login.LoginActivity;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private PreferencesManager preferencesManager;
    private AuthFlowManager authFlowManager;
    private final CompositeDisposable disposables = new CompositeDisposable();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        preferencesManager = ServiceLocator.getInstance().getPreferencesManager();
        authFlowManager = ServiceLocator.getInstance().getAuthFlowManager();

        bindProfile();
        setupActions();
        refreshProfile();
    }

    private void bindProfile() {
        if (binding == null) return;

        String userName = preferencesManager.getUserName();
        String userEmail = preferencesManager.getUserEmail();
        boolean isStaff = preferencesManager.isStaff();

        binding.tvWelcome.setText("WELCOME");
        binding.tvWelcomeSub.setText(userName != null && !userName.isEmpty()
                ? userName
                : getString(com.vaultpool.customer.R.string.guest_user));
        binding.tvEmailValue.setText(userEmail != null && !userEmail.isEmpty() ? userEmail : "-");
        binding.tvMoodFeedback.setText(getString(com.vaultpool.customer.R.string.mood_prompt));
        
        binding.btnStaff.setVisibility(isStaff ? View.VISIBLE : View.GONE);
    }

    private void setupActions() {
        binding.btnLogout.setOnClickListener(v -> logout());
        binding.btnStaff.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), com.vaultpool.customer.presentation.ui.staff.StaffActivity.class);
            startActivity(intent);
        });
        binding.btnMoodSad.setOnClickListener(v -> showMoodFeedback(com.vaultpool.customer.R.string.mood_feedback_sad));
        binding.btnMoodNeutral.setOnClickListener(v -> showMoodFeedback(com.vaultpool.customer.R.string.mood_feedback_neutral));
        binding.btnMoodHappy.setOnClickListener(v -> showMoodFeedback(com.vaultpool.customer.R.string.mood_feedback_happy));
    }

    private void showMoodFeedback(int messageRes) {
        if (binding == null) return;
        binding.tvMoodFeedback.setText(getString(messageRes));
    }

    private void refreshProfile() {
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
            Toast.makeText(requireContext(), getString(com.vaultpool.customer.R.string.session_expired), Toast.LENGTH_LONG).show();
            Intent intent = new Intent(requireContext(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            requireActivity().finish();
        } else if (message != null && !message.isEmpty()) {
            Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
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
                            Intent intent = new Intent(requireContext(), LoginActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            requireActivity().finish();
                        }, throwable -> {
                            setLoading(false);
                            Toast.makeText(requireContext(), throwable.getMessage(), Toast.LENGTH_LONG).show();
                        })
        );
    }

    private void setLoading(boolean loading) {
        if (binding != null) {
            binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
            binding.btnLogout.setEnabled(!loading);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        disposables.clear();
        binding = null;
    }
}
