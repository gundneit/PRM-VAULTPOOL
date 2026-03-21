package com.vaultpool.customer.presentation.ui.onboarding;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.vaultpool.customer.R;
import com.vaultpool.customer.databinding.FragmentOnboardingPageBinding;

public class OnboardingPageFragment extends Fragment {

    private static final String ARG_PAGE = "arg_page";

    private FragmentOnboardingPageBinding binding;

    public static OnboardingPageFragment newInstance(int page) {
        OnboardingPageFragment fragment = new OnboardingPageFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, page);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentOnboardingPageBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bindPageContent();
    }

    private void bindPageContent() {
        int page = getArguments() != null ? getArguments().getInt(ARG_PAGE, 0) : 0;

        switch (page) {
            case 1:
                binding.rootLayout.setBackgroundResource(R.drawable.bg_onboarding_page_2);
                binding.tvTitle.setText(R.string.onboarding_page_two_title);
                binding.tvDescription.setText(R.string.onboarding_page_two_desc);
                bindOnboardingImage("onboarding-2.png");
                break;
            case 2:
                binding.rootLayout.setBackgroundResource(R.drawable.bg_onboarding_page_3);
                binding.tvTitle.setText(R.string.onboarding_page_three_title);
                binding.tvDescription.setText(R.string.onboarding_page_three_desc);
                bindOnboardingImage("onboarding-3.png");
                break;
            case 0:
            default:
                binding.rootLayout.setBackgroundResource(R.drawable.bg_onboarding_page_1);
                binding.tvTitle.setText(R.string.onboarding_page_one_title);
                binding.tvDescription.setText(R.string.onboarding_page_one_desc);
                bindOnboardingImage("onboarding-1.png");
                break;
        }
    }

    private void bindOnboardingImage(String fileName) {
        try (java.io.InputStream inputStream = requireContext().getAssets().open("images/" + fileName)) {
            binding.ivHeroImage.setImageBitmap(BitmapFactory.decodeStream(inputStream));
            binding.ivHeroImage.setVisibility(View.VISIBLE);
        } catch (Exception ignored) {
            binding.ivHeroImage.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}