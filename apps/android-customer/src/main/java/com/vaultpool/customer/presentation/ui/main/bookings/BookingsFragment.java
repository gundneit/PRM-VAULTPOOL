package com.vaultpool.customer.presentation.ui.main.bookings;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.vaultpool.customer.ServiceLocator;
import com.vaultpool.customer.data.local.prefs.PreferencesManager;
import com.vaultpool.customer.data.remote.api.BookingApi;
import com.vaultpool.customer.data.remote.api.PaymentApi;
import com.vaultpool.customer.data.remote.dto.BookingResponseDto;
import com.vaultpool.customer.data.remote.dto.CreatePaymentRequestDto;
import com.vaultpool.customer.databinding.FragmentBookingsBinding;
import com.vaultpool.customer.presentation.ui.payment.ZaloPaymentActivity;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class BookingsFragment extends Fragment {

    private FragmentBookingsBinding binding;
    private BookingApi bookingApi;
    private PreferencesManager preferencesManager;
    private BookingItemAdapter adapter;
    private final CompositeDisposable disposables = new CompositeDisposable();

    private String currentFilter = null;
    private boolean isFirstLoad = true;
    public static String pendingTabStatus = null;
    public static Long pendingPaidBookingId = null;
    private Long recentlyPaidBookingId = null;
    private long recentlyPaidUntilMs = 0L;

    // Polling config — chờ webhook BE xác nhận sau thanh toán thành công
    private static final int POLL_INTERVAL_MS = 2000;
    private static final int POLL_MAX_COUNT = 30;
    private static final long RECENTLY_PAID_GUARD_MS = 120000L;
    private int pollCount = 0;
    private boolean isPolling = false;

    // =========================================================================
    // Lifecycle
    // =========================================================================

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentBookingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        bookingApi = ServiceLocator.getInstance().getBookingApi();
        preferencesManager = ServiceLocator.getInstance().getPreferencesManager();

        setupRecyclerView();
        setupFilterChips();

        if (pendingTabStatus != null) {
            String status = pendingTabStatus;
            pendingTabStatus = null;
            currentFilter = status;
            setChipChecked(status);
            consumePendingPaidBooking();

            if ("CONFIRMED".equals(status)) {
                // Polling để chờ webhook BE xác nhận booking sau thanh toán ZaloPay
                pollForConfirmed();
            } else {
                fetchBookings(status);
            }
        } else {
            currentFilter = "CONFIRMED";
            binding.chipConfirmed.setChecked(true);
            fetchBookings("CONFIRMED");
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (pendingTabStatus != null) {
            // Fallback: fragment cũ còn tồn tại, onViewCreated không chạy lại
            String status = pendingTabStatus;
            pendingTabStatus = null;
            consumePendingPaidBooking();
            selectTab(status);
        } else if (!isFirstLoad && !isPolling) {
            // Refresh khi user quay lại tab từ tab khác
            // Guard !isPolling để không interrupt polling sau ZaloPay
            fetchBookings(currentFilter);
        }

        isFirstLoad = false;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        isPolling = false;
        disposables.clear();
        binding = null;
    }

    // =========================================================================
    // Public API — dùng bởi MainActivity.handleNavigationIntent
    // =========================================================================

    public void selectTab(String status) {
        currentFilter = status;
        setChipChecked(status);
        consumePendingPaidBooking();

        if ("CONFIRMED".equals(status)) {
            pollForConfirmed();
        } else {
            fetchBookings(status);
        }
    }

    private void consumePendingPaidBooking() {
        if (pendingPaidBookingId != null && pendingPaidBookingId > 0) {
            recentlyPaidBookingId = pendingPaidBookingId;
            recentlyPaidUntilMs = System.currentTimeMillis() + RECENTLY_PAID_GUARD_MS;
            pendingPaidBookingId = null;
        }
    }

    // =========================================================================
    // Setup
    // =========================================================================

    private void setupRecyclerView() {
        adapter = new BookingItemAdapter(new ArrayList<>(), this::onBookingItemClick);
        binding.rvBookings.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvBookings.setAdapter(adapter);
    }

    private void setupFilterChips() {
        binding.chipAll.setOnClickListener(v -> {
            selectTab(null);
        });
        binding.chipConfirmed.setOnClickListener(v -> {
            selectTab("CONFIRMED");
        });
        binding.chipCheckedIn.setOnClickListener(v -> {
            selectTab("CHECKED_IN");
        });
        binding.chipPending.setOnClickListener(v -> {
            selectTab("PENDING_PAYMENT");
        });
        binding.chipExpired.setOnClickListener(v -> {
            selectTab("EXPIRED");
        });
    }

    // =========================================================================
    // UI helpers
    // =========================================================================

    private void setChipChecked(String status) {
        binding.chipAll.setChecked(false);
        binding.chipConfirmed.setChecked(false);
        binding.chipCheckedIn.setChecked(false);
        binding.chipPending.setChecked(false);
        binding.chipExpired.setChecked(false);

        if (status == null) {
            binding.chipAll.setChecked(true);
            return;
        }
        switch (status) {
            case "CONFIRMED":
                binding.chipConfirmed.setChecked(true);
                break;
            case "PENDING_PAYMENT":
                binding.chipPending.setChecked(true);
                break;
            case "CHECKED_IN":
                binding.chipCheckedIn.setChecked(true);
                break;
            case "EXPIRED":
                binding.chipExpired.setChecked(true);
                break;
            default:
                binding.chipAll.setChecked(true);
                break;
        }
    }

    private void setLoading(boolean loading) {
        if (binding == null)
            return;
        binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    // =========================================================================
    // Polling — chờ webhook BE xác nhận sau ZaloPay thành công
    // Poll mỗi 2s, tối đa 30 lần (60s). Nếu hết lượt → fallback fetchBookings.
    // =========================================================================

    private void pollForConfirmed() {
        if (isPolling)
            return;
        isPolling = true;
        pollCount = 0;
        setLoading(true);
        schedulePoll();
    }

    private void schedulePoll() {
        if (binding == null)
            return;
        binding.getRoot().postDelayed(() -> {
            if (binding == null || !isPolling)
                return;

            String token = preferencesManager.getFirebaseToken();
            if (token == null) {
                isPolling = false;
                setLoading(false);
                return;
            }
            String bearer = token.startsWith("Bearer ") ? token : "Bearer " + token;

            disposables.add(
                    bookingApi.getMyBookings(bearer, "CONFIRMED")
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(response -> {
                                if (binding == null)
                                    return;

                                boolean hasConfirmed = response.isSuccess()
                                        && response.getData() != null
                                        && hasConfirmedBooking(response.getData());

                                if (hasConfirmed) {
                                    // Webhook đã xác nhận → hiển thị, dừng poll
                                    isPolling = false;
                                    setLoading(false);
                                    List<BookingResponseDto> list = response.getData();
                                    adapter.updateItems(list);
                                    binding.tvEmpty.setVisibility(View.GONE);
                                    binding.rvBookings.setVisibility(View.VISIBLE);
                                } else if (pollCount < POLL_MAX_COUNT) {
                                    // Chưa có CONFIRMED → thử lại
                                    pollCount++;
                                    schedulePoll();
                                } else {
                                    // Hết lượt poll → fetch bình thường, dừng loading
                                    isPolling = false;
                                    fetchBookings("CONFIRMED");
                                }
                            }, error -> {
                                isPolling = false;
                                fetchBookings("CONFIRMED");
                            }));
        }, POLL_INTERVAL_MS);
    }

    // =========================================================================
    // Data
    // =========================================================================

    private void fetchBookings(String status) {
        String token = preferencesManager.getFirebaseToken();
        if (token == null)
            return;
        String bearer = token.startsWith("Bearer ") ? token : "Bearer " + token;

        setLoading(true);

        disposables.add(
                bookingApi.getMyBookings(bearer, status)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(response -> {
                            setLoading(false);
                            if (binding == null)
                                return;
                            if (response.isSuccess() && response.getData() != null) {
                                List<BookingResponseDto> list = response.getData();
                                adapter.updateItems(list);
                                binding.tvEmpty.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
                                binding.rvBookings.setVisibility(list.isEmpty() ? View.GONE : View.VISIBLE);
                            }
                        }, error -> {
                            setLoading(false);
                            if (getContext() != null) {
                                Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }));
    }

    // =========================================================================
    // Item interactions
    // =========================================================================

    private void onBookingItemClick(BookingResponseDto booking) {
        if ("PENDING_PAYMENT".equals(booking.getStatus())) {
            if (isRecentlyPaidBooking(booking)) {
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Đang xử lý")
                        .setMessage(
                                "Đơn này vừa thanh toán thành công, hệ thống đang chờ webhook xác nhận. Vui lòng đợi thêm một lúc rồi tải lại.")
                        .setPositiveButton("OK", null)
                        .show();
                return;
            }

            // Guard: paymentStatus SUCCESS = đang chờ webhook, không cho resume
            if ("SUCCESS".equals(booking.getPaymentStatus())) {
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Đang xử lý")
                        .setMessage(
                                "Thanh toán đã được ghi nhận, đang chờ xác nhận từ hệ thống. Vui lòng thử lại sau ít phút.")
                        .setPositiveButton("OK", null)
                        .show();
                return;
            }
            showResumePaymentDialog(booking);
        }
    }

    private boolean isRecentlyPaidBooking(BookingResponseDto booking) {
        if (booking == null || booking.getId() == null || recentlyPaidBookingId == null)
            return false;
        if (System.currentTimeMillis() > recentlyPaidUntilMs)
            return false;
        return recentlyPaidBookingId.equals(booking.getId());
    }

    private boolean hasConfirmedBooking(List<BookingResponseDto> list) {
        if (list == null || list.isEmpty())
            return false;
        if (recentlyPaidBookingId == null)
            return true;

        for (BookingResponseDto item : list) {
            if (item != null && recentlyPaidBookingId.equals(item.getId())) {
                return true;
            }
        }
        return false;
    }

    private void showResumePaymentDialog(BookingResponseDto booking) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Tiếp tục thanh toán?")
                .setMessage("Booking " + booking.getBookingCode()
                        + " chưa được thanh toán.\nBạn có muốn tiếp tục không?")
                .setPositiveButton("Tiếp tục", (dialog, which) -> resumePayment(booking))
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void resumePayment(BookingResponseDto booking) {
        String token = preferencesManager.getFirebaseToken();
        if (token == null)
            return;
        String bearer = token.startsWith("Bearer ") ? token : "Bearer " + token;

        PaymentApi paymentApi = ServiceLocator.getInstance().getPaymentApi();

        setLoading(true);

        disposables.add(
                paymentApi.createPayment(bearer,
                        new CreatePaymentRequestDto(booking.getId(), "ZALOPAY"))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(response -> {
                            setLoading(false);
                            if (response.isSuccess() && response.getData() != null) {
                                Intent intent = new Intent(requireContext(), ZaloPaymentActivity.class);
                                intent.putExtra(ZaloPaymentActivity.EXTRA_ZP_TRANS_TOKEN,
                                        response.getData().getRedirectUrl());
                                intent.putExtra(ZaloPaymentActivity.EXTRA_BOOKING_ID,
                                        response.getData().getBookingId());
                                startActivity(intent);
                            } else {
                                Toast.makeText(getContext(), response.getMessage(), Toast.LENGTH_LONG).show();
                                fetchBookings(currentFilter);
                            }
                        }, error -> {
                            setLoading(false);
                            if (getContext() != null) {
                                Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                                fetchBookings(currentFilter);
                            }
                        }));
    }
}