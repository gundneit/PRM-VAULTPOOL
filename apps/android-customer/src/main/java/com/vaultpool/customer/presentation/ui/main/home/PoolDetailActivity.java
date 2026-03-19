package com.vaultpool.customer.presentation.ui.main.home;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.vaultpool.customer.ServiceLocator;
import com.vaultpool.customer.data.remote.dto.PoolDto;
import com.vaultpool.customer.data.remote.dto.SlotDto;
import com.vaultpool.customer.databinding.ActivityPoolDetailBinding;
import com.vaultpool.customer.databinding.DialogBookSlotsBinding;
import com.vaultpool.customer.domain.repository.PoolRepository;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import java.util.List;
import java.util.Locale;

public class PoolDetailActivity extends AppCompatActivity {

    public static final String EXTRA_POOL_ID = "extra_pool_id";
    private ActivityPoolDetailBinding binding;
    private PoolRepository poolRepository;
    private final CompositeDisposable disposables = new CompositeDisposable();
    private Long poolId;
    private List<SlotDto> availableSlots;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPoolDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        poolId = getIntent().getLongExtra(EXTRA_POOL_ID, -1);
        if (poolId == -1) {
            finish();
            return;
        }

        poolRepository = ServiceLocator.getInstance().getPoolRepository();
        
        setupToolbar();
        setupClickListeners();
        fetchPoolDetail();
        fetchSlots();
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupClickListeners() {
        binding.btnMainBook.setOnClickListener(v -> showBookingDialog());
    }

    private void fetchPoolDetail() {
        disposables.add(
                poolRepository.getPoolDetail(poolId)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(response -> {
                            if (response.isSuccess() && response.getData() != null) {
                                displayPoolDetail(response.getData());
                            }
                        }, throwable -> {
                            Toast.makeText(this, "Error: " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                        })
        );
    }

    private void displayPoolDetail(PoolDto pool) {
        binding.tvDetailName.setText(pool.getName());
        binding.tvDetailAddress.setText(pool.getAddress());
        binding.tvDetailDescription.setText(pool.getDescription());
        
        // Hiển thị giờ mở cửa
        if (pool.getOpenHours() != null) {
            binding.tvDetailOpenHours.setText("Open: " + pool.getOpenHours());
            binding.tvDetailOpenHours.setVisibility(View.VISIBLE);
        } else {
            binding.tvDetailOpenHours.setVisibility(View.GONE);
        }

        // Hiển thị kinh độ vĩ độ
        if (pool.getGeoLat() != null && pool.getGeoLng() != null) {
            binding.tvDetailCoords.setText(String.format(Locale.getDefault(), "Coords: %.4f, %.4f", pool.getGeoLat(), pool.getGeoLng()));
            binding.tvDetailCoords.setVisibility(View.VISIBLE);
        } else {
            binding.tvDetailCoords.setVisibility(View.GONE);
        }

        if (pool.getImages() != null && !pool.getImages().isEmpty()) {
            Glide.with(this)
                    .load(pool.getImages().get(0).getImageUrl())
                    .into(binding.ivPoolLarge);
        }
    }

    private void fetchSlots() {
        // Hardcoded date for demonstration as per swagger example
        String today = "2026-03-19"; 
        
        disposables.add(
                poolRepository.getSlotsByPoolAndDate(poolId, today)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(response -> {
                            if (response.isSuccess() && response.getData() != null) {
                                this.availableSlots = response.getData();
                                setupSlotsRecyclerView(response.getData());
                            }
                        }, throwable -> {
                            Toast.makeText(this, "Error loading slots: " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                        })
        );
    }

    private void setupSlotsRecyclerView(List<SlotDto> slots) {
        SlotAdapter adapter = new SlotAdapter(slots, slot -> {
            handleBooking(slot);
        });
        binding.rvSlots.setLayoutManager(new LinearLayoutManager(this));
        binding.rvSlots.setAdapter(adapter);
    }

    private void showBookingDialog() {
        if (availableSlots == null || availableSlots.isEmpty()) {
            Toast.makeText(this, "No slots available for booking", Toast.LENGTH_SHORT).show();
            return;
        }

        BottomSheetDialog dialog = new BottomSheetDialog(this);
        DialogBookSlotsBinding dialogBinding = DialogBookSlotsBinding.inflate(getLayoutInflater());
        dialog.setContentView(dialogBinding.getRoot());

        dialogBinding.tvSelectedDate.setText("Date: 2026-03-19");
        
        SlotAdapter adapter = new SlotAdapter(availableSlots, slot -> {
            dialog.dismiss();
            handleBooking(slot);
        });
        
        dialogBinding.rvDialogSlots.setLayoutManager(new LinearLayoutManager(this));
        dialogBinding.rvDialogSlots.setAdapter(adapter);

        dialog.show();
    }

    private void handleBooking(SlotDto slot) {
        Toast.makeText(this, "Booking slot: " + slot.getStartTime(), Toast.LENGTH_SHORT).show();
        // Here you would navigate to a booking confirmation or call a booking API
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disposables.clear();
        binding = null;
    }
}
