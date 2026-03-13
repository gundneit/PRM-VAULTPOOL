package com.vaultpool.customer.presentation.ui.onboarding;

import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.vaultpool.customer.R;
import com.vaultpool.customer.databinding.ActivityOnboardingBinding;
import com.vaultpool.customer.presentation.ui.login.LoginActivity;

public class OnboardingActivity extends AppCompatActivity {

    private static final int PAGE_COUNT = 3;

    private ActivityOnboardingBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOnboardingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupViewPager();
        setupIndicators();
        setupActions();
        updateUiForPage(0);
    }

    private void setupViewPager() {
        binding.viewPager.setAdapter(new OnboardingPagerAdapter(this));
        binding.viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateUiForPage(position);
            }
        });
    }

    private void setupIndicators() {
        int smallSize = dpToPx(8);
        int spacing = dpToPx(8);

        for (int index = 0; index < PAGE_COUNT; index++) {
            View indicator = new View(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(smallSize, smallSize);
            params.setMargins(spacing / 2, 0, spacing / 2, 0);
            indicator.setLayoutParams(params);
            indicator.setBackgroundResource(R.drawable.indicator_inactive);
            binding.layoutIndicators.addView(indicator);
        }
    }

    private void setupActions() {
        binding.btnNext.setOnClickListener(v -> {
            int currentPage = binding.viewPager.getCurrentItem();
            if (currentPage < PAGE_COUNT - 1) {
                binding.viewPager.setCurrentItem(currentPage + 1, true);
            } else {
                startActivity(new Intent(this, LoginActivity.class));
                finish();
            }
        });
    }

    private void updateUiForPage(int position) {
        updateIndicators(position);
        binding.btnNext.setText(position == PAGE_COUNT - 1 ? R.string.get_started : R.string.next);
    }

    private void updateIndicators(int selectedPosition) {
        int activeWidth = dpToPx(24);
        int inactiveSize = dpToPx(8);

        for (int index = 0; index < binding.layoutIndicators.getChildCount(); index++) {
            View indicator = binding.layoutIndicators.getChildAt(index);
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) indicator.getLayoutParams();
            if (index == selectedPosition) {
                params.width = activeWidth;
                params.height = inactiveSize;
                indicator.setBackgroundResource(R.drawable.indicator_active);
            } else {
                params.width = inactiveSize;
                params.height = inactiveSize;
                indicator.setBackgroundResource(R.drawable.indicator_inactive);
            }
            indicator.setLayoutParams(params);
        }
    }

    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                getResources().getDisplayMetrics()
        );
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}