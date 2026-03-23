package com.vaultpool.customer.presentation.ui.main.home;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.Gson;
import com.vaultpool.customer.R;
import com.vaultpool.customer.ServiceLocator;
import com.vaultpool.customer.BuildConfig;
import com.vaultpool.customer.data.remote.dto.ApiResponse;
import com.vaultpool.customer.data.remote.dto.PoolDto;
import com.vaultpool.customer.data.remote.dto.SlotDto;
import com.vaultpool.customer.databinding.ActivityPoolDetailBinding;
import com.vaultpool.customer.databinding.DialogBookSlotsBinding;
import com.vaultpool.customer.data.local.prefs.PreferencesManager;
import com.vaultpool.customer.data.remote.api.BookingApi;
import com.vaultpool.customer.data.remote.api.PaymentApi;
import com.vaultpool.customer.data.remote.dto.CreateBookingRequestDto;
import com.vaultpool.customer.data.remote.dto.CreatePaymentRequestDto;
import com.vaultpool.customer.domain.repository.PoolRepository;
import com.vaultpool.customer.presentation.ui.payment.ConfirmPaymentActivity;
import com.vaultpool.customer.presentation.ui.payment.ZaloPaymentActivity;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

import retrofit2.HttpException;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

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
    private PreferencesManager preferencesManager;
    private BookingApi bookingApi;
    private PaymentApi paymentApi;
    private final CompositeDisposable disposables = new CompositeDisposable();
    private Long poolId;
    private List<SlotDto> allSlots = new ArrayList<>();
    private Calendar selectedDate = Calendar.getInstance();
    private int guestCount = 1;
    private SlotDto selectedSlot = null;

    private TimeSlotAdapter morningAdapter;
    private TimeSlotAdapter afternoonAdapter;
    private AlertDialog loadingDialog;

    private MapView mapView;
    private GoogleMap googleMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPoolDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mapView = binding.mapView;
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(map -> {
            googleMap = map;
            googleMap.getUiSettings().setAllGesturesEnabled(false);
            googleMap.getUiSettings().setZoomControlsEnabled(false);
        });

        poolId = getIntent().getLongExtra(EXTRA_POOL_ID, -1);
        if (poolId == -1) {
            finish();
            return;
        }

        poolRepository = ServiceLocator.getInstance().getPoolRepository();
        preferencesManager = ServiceLocator.getInstance().getPreferencesManager();
        bookingApi = ServiceLocator.getInstance().getBookingApi();

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .addInterceptor(chain -> {
                    String token = preferencesManager.getFirebaseToken();
                    okhttp3.Request original = chain.request();
                    if (token != null && !token.isEmpty()) {
                        return chain.proceed(original.newBuilder()
                                .header("Authorization", "Bearer " + token)
                                .build());
                    }
                    return chain.proceed(original);
                })
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BuildConfig.BACKEND_BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                .build();
        paymentApi = retrofit.create(PaymentApi.class);

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
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupClickListeners() {
        binding.btnMainBook.setOnClickListener(v -> showBookingDialog());
        binding.btnAddToCart.setOnClickListener(v -> showAddToCartDialog());
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
                        }, throwable -> handleError(throwable, "Error fetching pool detail"))
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
            binding.mapView.setVisibility(View.VISIBLE);
            if (googleMap != null) {
                showPoolOnMap(pool.getGeoLat(), pool.getGeoLng(), pool.getName());
            } else {
                mapView.getMapAsync(map -> {
                    googleMap = map;
                    googleMap.getUiSettings().setAllGesturesEnabled(false);
                    googleMap.getUiSettings().setZoomControlsEnabled(false);
                    showPoolOnMap(pool.getGeoLat(), pool.getGeoLng(), pool.getName());
                });
            }
        } else {
            binding.mapView.setVisibility(View.GONE);
        }

        if (pool.getImages() != null && !pool.getImages().isEmpty()) {
            Glide.with(this)
                    .load(pool.getImages().get(0).getImageUrl())
                    .into(binding.ivPoolLarge);
        }
    }

    private void showPoolOnMap(Double lat, Double lng, String poolName) {
        if (lat == null || lng == null || googleMap == null) return;
        LatLng position = new LatLng(lat, lng);
        googleMap.clear();
        googleMap.addMarker(new MarkerOptions()
                .position(position)
                .title(poolName));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 15f));
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
                        }, throwable -> handleError(throwable, "Error loading slots"))
        );
    }

    private void setupSlotsRecyclerView(List<SlotDto> slots) {
        SlotAdapter adapter = new SlotAdapter(slots, slot -> showBookingDialog());
        binding.rvSlots.setLayoutManager(new LinearLayoutManager(this));
        binding.rvSlots.setAdapter(adapter);
    }

    private void showBookingDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        DialogBookSlotsBinding dialogBinding = DialogBookSlotsBinding.inflate(getLayoutInflater());
        dialog.setContentView(dialogBinding.getRoot());

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
            selectedSlot = null;
            dialogBinding.tvMonthYear.setText(monthFormat.format(date.getTime()));
            fetchSlotsForDialog(dialogBinding);
            updateFooter(dialogBinding);
        });
        dialogBinding.rvDates.setAdapter(dateAdapter);

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

        dialogBinding.rvMorningSlots.setLayoutManager(new GridLayoutManager(this, 3));
        dialogBinding.rvAfternoonSlots.setLayoutManager(new GridLayoutManager(this, 3));

        morningAdapter = new TimeSlotAdapter(morningSlots, (slot, adapter) -> {
            selectedSlot = slot;
            afternoonAdapter.clearSelection();
            updateFooter(dialogBinding);
        });
        dialogBinding.rvMorningSlots.setAdapter(morningAdapter);

        afternoonAdapter = new TimeSlotAdapter(afternoonSlots, (slot, adapter) -> {
            selectedSlot = slot;
            morningAdapter.clearSelection();
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

    private void showAddToCartDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        DialogBookSlotsBinding dialogBinding = DialogBookSlotsBinding.inflate(getLayoutInflater());
        dialog.setContentView(dialogBinding.getRoot());

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
            selectedSlot = null;
            dialogBinding.tvMonthYear.setText(monthFormat.format(date.getTime()));
            fetchSlotsForDialog(dialogBinding);
            updateFooter(dialogBinding);
        });
        dialogBinding.rvDates.setAdapter(dateAdapter);

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

        displaySlotsInDialog(allSlots, dialogBinding);
        updateFooter(dialogBinding);

        dialogBinding.btnContinuePayment.setText("Add to Cart");
        dialogBinding.btnContinuePayment.setOnClickListener(v -> {
            if (selectedSlot == null) {
                Toast.makeText(this, "Please select a time slot", Toast.LENGTH_SHORT).show();
                return;
            }
            addToCart(selectedSlot);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void addToCart(SlotDto slot) {
        if (preferencesManager.getFirebaseToken() == null) {
            Toast.makeText(this, "Please login to add to cart", Toast.LENGTH_SHORT).show();
            return;
        }

        String token = formatToken(preferencesManager.getFirebaseToken());
        CreateBookingRequestDto request = new CreateBookingRequestDto(slot.getId(), guestCount, "IN_CART");

        disposables.add(
                bookingApi.createBooking(token, request)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(response -> {
                            if (response.isSuccess()) {
                                Toast.makeText(this, "Added to cart!", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(this, response.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }, throwable -> handleError(throwable, "Failed to add to cart"))
        );
    }

    private void handleBooking(SlotDto slot) {
        if (preferencesManager.getFirebaseToken() == null) {
            Toast.makeText(this, "Please login to book", Toast.LENGTH_SHORT).show();
            return;
        }

        String token = formatToken(preferencesManager.getFirebaseToken());
        CreateBookingRequestDto bookingRequest = new CreateBookingRequestDto(
                slot.getId(), guestCount, "PENDING_PAYMENT");

        showRedirectingDialog();

        disposables.add(
                bookingApi.createBooking(token, bookingRequest)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(bookingResp -> {
                            dismissRedirectingDialog();
                            if (bookingResp.isSuccess() && bookingResp.getData() != null) {
                                Intent intent = new Intent(this, ConfirmPaymentActivity.class);
                                intent.putExtra(ConfirmPaymentActivity.EXTRA_BOOKING_ID,
                                        bookingResp.getData().getId());
                                startActivity(intent);
                            } else {
                                Toast.makeText(this, bookingResp.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }, throwable -> {
                            dismissRedirectingDialog();
                            handleError(throwable, "Booking failed");
                        })
        );
    }

    private void handleError(Throwable throwable, String defaultMsg) {
        String errorMsg = defaultMsg;
        if (throwable instanceof HttpException) {
            try {
                okhttp3.ResponseBody responseBody = ((HttpException) throwable).response().errorBody();
                if (responseBody != null) {
                    String errorBody = responseBody.string();
                    ApiResponse<?> response = new Gson().fromJson(errorBody, ApiResponse.class);
                    if (response != null && response.getMessage() != null) {
                        errorMsg = response.getMessage();
                    }
                }
            } catch (Exception e) {
                errorMsg = throwable.getMessage();
            }
        } else if (throwable != null) {
            errorMsg = throwable.getMessage();
        }
        Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
        Log.e("PoolDetailActivity", "Error: " + errorMsg, throwable);
    }

    private String formatToken(String token) {
        if (token == null) return "";
        return token.startsWith("Bearer ") ? token : "Bearer " + token;
    }

    private void showRedirectingDialog() {
        loadingDialog = new MaterialAlertDialogBuilder(this)
                .setView(R.layout.dialog_redirecting)
                .setCancelable(false)
                .create();
        loadingDialog.show();
    }

    private void dismissRedirectingDialog() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }

    // ── MapView lifecycle ──────────────────────────────────────────────────────

    @Override
    protected void onStart() {
        super.onStart();
        if (mapView != null) mapView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mapView != null) mapView.onResume();
        dismissRedirectingDialog();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mapView != null) mapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mapView != null) mapView.onStop();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mapView != null) mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mapView != null) mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        if (mapView != null) mapView.onDestroy();
        disposables.clear();
        super.onDestroy();
        binding = null;
    }
}