package com.vaultpool.customer.presentation.ui.main.home;

import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
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
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class PoolDetailActivity extends AppCompatActivity {

    public static final String EXTRA_POOL_ID = "extra_pool_id";
    private ActivityPoolDetailBinding binding;
    private PoolRepository poolRepository;
    private final CompositeDisposable disposables = new CompositeDisposable();
    private Long poolId;
    private List<SlotDto> allSlots = new ArrayList<>();
    private Calendar selectedDate = Calendar.getInstance();
    private int guestCount = 1;
    private SlotDto selectedSlot = null;
    
    // Lưu trữ adapter để có thể xóa selection chéo nhau
    private TimeSlotAdapter morningAdapter;
    private TimeSlotAdapter afternoonAdapter;

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
        fetchSlots(selectedDate);
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
        
        if (pool.getOpenHours() != null) {
            binding.tvDetailOpenHours.setText("Open: " + pool.getOpenHours());
            binding.tvDetailOpenHours.setVisibility(View.VISIBLE);
        }

        if (pool.getGeoLat() != null && pool.getGeoLng() != null) {
            binding.tvDetailCoords.setText(String.format(Locale.getDefault(), "Coords: %.4f, %.4f", pool.getGeoLat(), pool.getGeoLng()));
            binding.tvDetailCoords.setVisibility(View.VISIBLE);
        }

        if (pool.getImages() != null && !pool.getImages().isEmpty()) {
            Glide.with(this)
                    .load(pool.getImages().get(0).getImageUrl())
                    .into(binding.ivPoolLarge);
        }
    }

    private void fetchSlots(Calendar date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        String dateStr = sdf.format(date.getTime());
        
        disposables.add(
                poolRepository.getSlotsByPoolAndDate(poolId, dateStr)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(response -> {
                            if (response.isSuccess() && response.getData() != null) {
                                this.allSlots = response.getData();
                                setupSlotsRecyclerView(response.getData());
                            }
                        }, throwable -> {
                            Toast.makeText(this, "Error loading slots: " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                        })
        );
    }

    private void setupSlotsRecyclerView(List<SlotDto> slots) {
        SlotAdapter adapter = new SlotAdapter(slots, slot -> {
            showBookingDialog();
        });
        binding.rvSlots.setLayoutManager(new LinearLayoutManager(this));
        binding.rvSlots.setAdapter(adapter);
    }

    private void showBookingDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        DialogBookSlotsBinding dialogBinding = DialogBookSlotsBinding.inflate(getLayoutInflater());
        dialog.setContentView(dialogBinding.getRoot());

        // Reset selection when opening dialog
        selectedSlot = null;

        dialog.setOnShowListener(dialogInterface -> {
            BottomSheetDialog d = (BottomSheetDialog) dialogInterface;
            FrameLayout bottomSheet = d.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                BottomSheetBehavior<FrameLayout> behavior = BottomSheetBehavior.from(bottomSheet);
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                behavior.setSkipCollapsed(true);
            }
        });

        // 1. Setup Date Selection
        List<Calendar> dates = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        for (int i = 0; i < 7; i++) {
            Calendar c = (Calendar) cal.clone();
            c.add(Calendar.DAY_OF_YEAR, i);
            dates.add(c);
        }
        
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM yyyy", Locale.ENGLISH);
        dialogBinding.tvMonthYear.setText(monthFormat.format(selectedDate.getTime()));

        DateAdapter dateAdapter = new DateAdapter(dates, date -> {
            selectedDate = date;
            selectedSlot = null; // Bỏ chọn slot khi đổi ngày
            dialogBinding.tvMonthYear.setText(monthFormat.format(date.getTime()));
            fetchSlotsForDialog(dialogBinding);
            updateFooter(dialogBinding);
        });
        dialogBinding.rvDates.setAdapter(dateAdapter);

        // 2. Setup Guest Selection
        dialogBinding.tvGuestCount.setText(String.valueOf(guestCount));
        dialogBinding.btnMinus.setOnClickListener(v -> {
            if (guestCount > 1) {
                guestCount--;
                dialogBinding.tvGuestCount.setText(String.valueOf(guestCount));
                updateFooter(dialogBinding);
            }
        });
        dialogBinding.btnPlus.setOnClickListener(v -> {
            guestCount++;
            dialogBinding.tvGuestCount.setText(String.valueOf(guestCount));
            updateFooter(dialogBinding);
        });

        // 3. Initial load
        displaySlotsInDialog(allSlots, dialogBinding);
        updateFooter(dialogBinding);

        dialogBinding.btnContinuePayment.setOnClickListener(v -> {
            if (selectedSlot == null) {
                Toast.makeText(this, "Please select a time slot", Toast.LENGTH_SHORT).show();
                return;
            }
            handleBooking(selectedSlot);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void fetchSlotsForDialog(DialogBookSlotsBinding dialogBinding) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        String dateStr = sdf.format(selectedDate.getTime());
        
        disposables.add(
                poolRepository.getSlotsByPoolAndDate(poolId, dateStr)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(response -> {
                            if (response.isSuccess() && response.getData() != null) {
                                displaySlotsInDialog(response.getData(), dialogBinding);
                            }
                        }, throwable -> {})
        );
    }

    private void displaySlotsInDialog(List<SlotDto> slots, DialogBookSlotsBinding dialogBinding) {
        List<SlotDto> morningSlots = new ArrayList<>();
        List<SlotDto> afternoonSlots = new ArrayList<>();

        for (SlotDto slot : slots) {
            String time = slot.getStartTime().substring(11, 13);
            int hour = Integer.parseInt(time);
            if (hour < 12) morningSlots.add(slot);
            else afternoonSlots.add(slot);
        }

        morningAdapter = new TimeSlotAdapter(morningSlots, (slot, adapter) -> {
            selectedSlot = slot;
            afternoonAdapter.clearSelection(); // Bỏ chọn bên Afternoon
            updateFooter(dialogBinding);
        });
        dialogBinding.rvMorningSlots.setAdapter(morningAdapter);

        afternoonAdapter = new TimeSlotAdapter(afternoonSlots, (slot, adapter) -> {
            selectedSlot = slot;
            morningAdapter.clearSelection(); // Bỏ chọn bên Morning
            updateFooter(dialogBinding);
        });
        dialogBinding.rvAfternoonSlots.setAdapter(afternoonAdapter);
    }

    private void updateFooter(DialogBookSlotsBinding dialogBinding) {
        if (selectedSlot != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM dd", Locale.ENGLISH);
            String datePart = sdf.format(selectedDate.getTime());
            String timePart = selectedSlot.getStartTime().substring(11, 16);
            dialogBinding.tvFooterSelection.setText(datePart + " • " + timePart);
            
            double totalPrice = selectedSlot.getPrice() * guestCount;
            NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            dialogBinding.tvFooterPrice.setText(formatter.format(totalPrice));
        } else {
            dialogBinding.tvFooterSelection.setText("Select a slot");
            dialogBinding.tvFooterPrice.setText("0đ");
        }
    }

    private void handleBooking(SlotDto slot) {
        Toast.makeText(this, "Booking confirmed for " + guestCount + " guests at " + slot.getStartTime(), Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disposables.clear();
        binding = null;
    }
}
