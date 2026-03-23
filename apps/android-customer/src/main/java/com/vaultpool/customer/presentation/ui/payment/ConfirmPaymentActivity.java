package com.vaultpool.customer.presentation.ui.payment;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.vaultpool.customer.R;
import com.vaultpool.customer.ServiceLocator;
import com.vaultpool.customer.data.local.prefs.PreferencesManager;
import com.vaultpool.customer.data.remote.api.BookingApi;
import com.vaultpool.customer.data.remote.api.PaymentApi;
import com.vaultpool.customer.data.remote.dto.BookingResponseDto;
import com.vaultpool.customer.data.remote.dto.CreatePaymentRequestDto;
import com.vaultpool.customer.databinding.ActivityConfirmPaymentBinding;

import java.text.NumberFormat;
import java.util.Locale;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class ConfirmPaymentActivity extends AppCompatActivity {

    public static final String EXTRA_BOOKING_ID = "extra_booking_id";

    private ActivityConfirmPaymentBinding binding;
    private BookingApi bookingApi;
    private PaymentApi paymentApi;
    private PreferencesManager preferencesManager;
    private final CompositeDisposable disposables = new CompositeDisposable();
    private BookingResponseDto currentBooking;
    private AlertDialog loadingDialog;
    private boolean hasLaunchedPayment = false;
    private Long targetBookingId = -1L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityConfirmPaymentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Nhận ID từ intent nếu có
        targetBookingId = getIntent().getLongExtra(EXTRA_BOOKING_ID, -1L);

        bookingApi = ServiceLocator.getInstance().getBookingApi();
        paymentApi = ServiceLocator.getInstance().getPaymentApi();
        preferencesManager = ServiceLocator.getInstance().getPreferencesManager();

        setupToolbar();
        fetchPendingBooking();
        setupActions();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (hasLaunchedPayment) {
            finish();
        }
        dismissRedirectingDialog();
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void fetchPendingBooking() {
        String token = getToken();
        if (token == null) return;

        setLoading(true);

        disposables.add(
                bookingApi.getMyBookings(token, "PENDING_PAYMENT")
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(response -> {
                            setLoading(false);
                            if (response.isSuccess() && response.getData() != null && !response.getData().isEmpty()) {
                                if (targetBookingId != -1L) {
                                    // Tìm đúng booking theo ID được truyền sang
                                    for (BookingResponseDto b : response.getData()) {
                                        if (b.getId().equals(targetBookingId)) {
                                            currentBooking = b;
                                            break;
                                        }
                                    }
                                }
                                
                                // Nếu không tìm thấy theo ID hoặc không truyền ID, lấy cái mới nhất (đầu tiên)
                                if (currentBooking == null) {
                                    currentBooking = response.getData().get(0);
                                }
                                
                                displayBooking(currentBooking);
                            } else {
                                Toast.makeText(this, "Không tìm thấy booking đang chờ thanh toán.", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        }, error -> {
                            setLoading(false);
                            Toast.makeText(this, "Lỗi tải dữ liệu: " + error.getMessage(), Toast.LENGTH_LONG).show();
                            finish();
                        })
        );
    }

    private void displayBooking(BookingResponseDto booking) {
        binding.tvPoolName.setText(booking.getPoolName());
        binding.tvPoolAddress.setText(booking.getPoolAddress());
        binding.tvBookingCode.setText(booking.getBookingCode());
        binding.tvQty.setText(booking.getQty() + " vé");

        String start = formatTime(booking.getStartTime());
        String end = formatTime(booking.getEndTime());
        binding.tvTime.setText(start + " – " + end);

        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        long amount = booking.getAmount() != null ? booking.getAmount() : 0;
        binding.tvAmount.setText(formatter.format(amount));

        if (booking.getExpiresAt() != null && booking.getExpiresAt().length() >= 16) {
            binding.tvExpiresAt.setText("Hết hạn lúc: "
                    + booking.getExpiresAt().substring(11, 16)
                    + " ngày " + booking.getExpiresAt().substring(8, 10)
                    + "/" + booking.getExpiresAt().substring(5, 7));
            binding.tvExpiresAt.setVisibility(View.VISIBLE);
        }

        binding.contentLayout.setVisibility(View.VISIBLE);
    }

    private void setupActions() {
        binding.btnConfirmPayment.setOnClickListener(v -> {
            if (currentBooking == null) return;
            showRedirectingDialog();
            createPayment(currentBooking.getId());
        });

        binding.btnCancel.setOnClickListener(v ->
                new MaterialAlertDialogBuilder(this)
                        .setTitle("Hủy đặt chỗ?")
                        .setMessage("Booking này sẽ bị treo nếu bạn không thanh toán ngay.")
                        .setPositiveButton("Rời đi", (d, w) -> finish())
                        .setNegativeButton("Ở lại", null)
                        .show()
        );
    }

    private void createPayment(Long bookingId) {
        String token = getToken();
        if (token == null) return;

        disposables.add(
                paymentApi.createPayment(token,
                                new CreatePaymentRequestDto(bookingId, "ZALOPAY"))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(response -> {
                            dismissRedirectingDialog();
                            if (response.isSuccess() && response.getData() != null) {
                                hasLaunchedPayment = true;
                                Intent intent = new Intent(this, ZaloPaymentActivity.class);
                                intent.putExtra(ZaloPaymentActivity.EXTRA_ZP_TRANS_TOKEN,
                                        response.getData().getRedirectUrl());
                                intent.putExtra(ZaloPaymentActivity.EXTRA_BOOKING_ID,
                                        response.getData().getBookingId());
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(this, response.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }, error -> {
                            dismissRedirectingDialog();
                            Toast.makeText(this, "Lỗi tạo thanh toán: " + error.getMessage(), Toast.LENGTH_LONG).show();
                        })
        );
    }

    private String getToken() {
        String token = preferencesManager.getFirebaseToken();
        if (token == null) return null;
        return token.startsWith("Bearer ") ? token : "Bearer " + token;
    }

    private String formatTime(String iso) {
        if (iso == null || iso.length() < 16) return iso != null ? iso : "-";
        String time = iso.substring(11, 16);
        String date = iso.substring(8, 10) + "/" + iso.substring(5, 7);
        return time + " · " + date;
    }

    private void setLoading(boolean loading) {
        binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.contentLayout.setVisibility(loading ? View.GONE : View.VISIBLE);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disposables.clear();
        binding = null;
    }
}
