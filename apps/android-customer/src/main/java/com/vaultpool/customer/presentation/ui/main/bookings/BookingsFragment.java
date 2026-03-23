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
    public static String pendingTabStatus = null;

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

        currentFilter = "CONFIRMED";
        binding.chipConfirmed.setChecked(true);
        fetchBookings("CONFIRMED");
    }

    @Override
    public void onResume() {
        super.onResume();
        if (pendingTabStatus != null) {
            String status = pendingTabStatus;
            pendingTabStatus = null; // clear TRƯỚC khi selectTab tránh loop
            selectTab(status);
        } else {
            fetchBookings(currentFilter);
        }
    }

    public void selectTab(String status) {
        currentFilter = status;
        switch (status) {
            case "CONFIRMED":       binding.chipConfirmed.setChecked(true); break;
            case "PENDING_PAYMENT": binding.chipPending.setChecked(true);   break;
            case "CHECKED_IN":      binding.chipCheckedIn.setChecked(true); break;
            case "EXPIRED":         binding.chipExpired.setChecked(true);   break;
            default:                binding.chipAll.setChecked(true);       break;
        }
        fetchBookings(status);
    }

    private void setupRecyclerView() {
        adapter = new BookingItemAdapter(new ArrayList<>(), this::onBookingItemClick);
        binding.rvBookings.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvBookings.setAdapter(adapter);
    }

    private void onBookingItemClick(BookingResponseDto booking) {
        if ("PENDING_PAYMENT".equals(booking.getStatus())) {
            showResumePaymentDialog(booking);
        }
    }

    private void showResumePaymentDialog(BookingResponseDto booking) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Tiếp tục thanh toán?")
                .setMessage("Booking " + booking.getBookingCode() + " chưa được thanh toán.\nBạn có muốn tiếp tục không?")
                .setPositiveButton("Tiếp tục", (dialog, which) -> resumePayment(booking))
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void resumePayment(BookingResponseDto booking) {
        String token = preferencesManager.getFirebaseToken();
        if (token == null) return;
        String bearer = token.startsWith("Bearer ") ? token : "Bearer " + token;

        PaymentApi paymentApi = ServiceLocator.getInstance().getPaymentApi();

        disposables.add(
                paymentApi.createPayment(bearer,
                                new CreatePaymentRequestDto(booking.getId(), "ZALOPAY"))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(response -> {
                            if (response.isSuccess() && response.getData() != null) {
                                Intent intent = new Intent(requireContext(), ZaloPaymentActivity.class);
                                intent.putExtra(ZaloPaymentActivity.EXTRA_ZP_TRANS_TOKEN,
                                        response.getData().getRedirectUrl());
                                intent.putExtra(ZaloPaymentActivity.EXTRA_BOOKING_ID,
                                        response.getData().getBookingId());
                                startActivity(intent);
                            } else {
                                Toast.makeText(getContext(), response.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }, error -> {
                            Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                        })
        );
    }

    private void setupFilterChips() {
        binding.chipAll.setOnClickListener(v -> {
            currentFilter = null;
            fetchBookings(null);
        });
        binding.chipConfirmed.setOnClickListener(v -> {
            currentFilter = "CONFIRMED";
            fetchBookings("CONFIRMED");
        });
        binding.chipCheckedIn.setOnClickListener(v -> {
            currentFilter = "CHECKED_IN";
            fetchBookings("CHECKED_IN");
        });
        binding.chipPending.setOnClickListener(v -> {
            currentFilter = "PENDING_PAYMENT";
            fetchBookings("PENDING_PAYMENT");
        });
        binding.chipExpired.setOnClickListener(v -> {
            currentFilter = "EXPIRED";
            fetchBookings("EXPIRED");
        });
    }

    private void fetchBookings(String status) {
        String token = preferencesManager.getFirebaseToken();
        if (token == null) return;
        String bearer = token.startsWith("Bearer ") ? token : "Bearer " + token;

        setLoading(true);

        disposables.add(
                bookingApi.getMyBookings(bearer, status)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(response -> {
                            setLoading(false);
                            if (response.isSuccess() && response.getData() != null) {
                                List<BookingResponseDto> list = response.getData();
                                adapter.updateItems(list);
                                binding.tvEmpty.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
                                binding.rvBookings.setVisibility(list.isEmpty() ? View.GONE : View.VISIBLE);
                            }
                        }, error -> {
                            setLoading(false);
                            Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                        })
        );
    }

    private void setLoading(boolean loading) {
        binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        disposables.clear();
        binding = null;
    }
}