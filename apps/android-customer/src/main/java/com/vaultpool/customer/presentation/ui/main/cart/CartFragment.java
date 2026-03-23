package com.vaultpool.customer.presentation.ui.main.cart;

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

import com.google.gson.Gson;
import com.vaultpool.customer.ServiceLocator;
import com.vaultpool.customer.databinding.FragmentCartBinding;
import com.vaultpool.customer.data.local.prefs.PreferencesManager;
import com.vaultpool.customer.data.remote.api.BookingApi;
import com.vaultpool.customer.data.remote.dto.ApiResponse;
import com.vaultpool.customer.data.remote.dto.BookingResponseDto;
import com.vaultpool.customer.presentation.ui.payment.ConfirmPaymentActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import retrofit2.HttpException;

public class CartFragment extends Fragment implements CartItemAdapter.OnItemSelectionChangeListener {

    private FragmentCartBinding binding;
    private PreferencesManager preferencesManager;
    private BookingApi bookingApi;
    private final CompositeDisposable disposables = new CompositeDisposable();
    private CartItemAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCartBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        preferencesManager = ServiceLocator.getInstance().getPreferencesManager();
        bookingApi = ServiceLocator.getInstance().getBookingApi();

        setupRecyclerView();
        setupCheckoutButton();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (bookingApi != null) {
            fetchCart();
        }
    }

    private void setupRecyclerView() {
        adapter = new CartItemAdapter(new ArrayList<>(), this);
        binding.rvCart.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvCart.setAdapter(adapter);
    }

    @Override
    public void onSelectionChanged() {
        updateTotalAmount();
    }

    private void fetchCart() {
        String firebaseToken = preferencesManager.getFirebaseToken();
        if (firebaseToken == null) {
            showMessage("Please log in to view your cart");
            return;
        }
        String token = firebaseToken.startsWith("Bearer ") ? firebaseToken : "Bearer " + firebaseToken;
        
        setLoading(true);
        disposables.add(
                bookingApi.getCart(token)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(response -> {
                            setLoading(false);
                            if (response.isSuccess() && response.getData() != null) {
                                List<BookingResponseDto> cartItems = response.getData();
                                adapter.updateItems(cartItems);
                                updateUI(cartItems);
                            } else {
                                showMessage(response.getMessage());
                            }
                        }, error -> {
                            setLoading(false);
                            showMessage("Error loading cart: " + getErrorMessage(error));
                        })
        );
    }

    private void updateUI(List<BookingResponseDto> items) {
        if (items.isEmpty()) {
            binding.tvEmptyCart.setVisibility(View.VISIBLE);
            binding.rvCart.setVisibility(View.GONE);
            binding.layoutCheckout.setVisibility(View.GONE);
        } else {
            binding.tvEmptyCart.setVisibility(View.GONE);
            binding.rvCart.setVisibility(View.VISIBLE);
            binding.layoutCheckout.setVisibility(View.VISIBLE);
            updateTotalAmount();
        }
    }

    private void updateTotalAmount() {
        List<BookingResponseDto> items = adapter.getItems();
        long total = 0;
        int selectedCount = 0;
        if (items != null) {
            for (BookingResponseDto item : items) {
                if (item.isSelected()) {
                    total += item.getAmount() != null ? item.getAmount() : 0;
                    selectedCount++;
                }
            }
        }
        binding.tvTotalAmount.setText(String.format("₫ %,d", total));
        binding.btnCheckout.setEnabled(selectedCount > 0);
        binding.btnCheckout.setText(selectedCount > 0 ? "Checkout (" + selectedCount + ")" : "Checkout");
    }

    private void setupCheckoutButton() {
        binding.btnCheckout.setOnClickListener(v -> {
            performCheckout();
        });
    }

    private void performCheckout() {
        List<BookingResponseDto> allItems = adapter.getItems();
        if (allItems == null || allItems.isEmpty()) {
            showMessage("Cart is empty");
            return;
        }

        List<BookingResponseDto> selectedItems = allItems.stream()
                .filter(BookingResponseDto::isSelected)
                .collect(Collectors.toList());

        if (selectedItems.isEmpty()) {
            showMessage("Please select at least one item");
            return;
        }

        String firebaseToken = preferencesManager.getFirebaseToken();
        if (firebaseToken == null) return;
        String token = firebaseToken.startsWith("Bearer ") ? firebaseToken : "Bearer " + firebaseToken;

        setLoading(true);
        disposables.add(
                Observable.fromIterable(selectedItems)
                        .concatMapSingle(item -> bookingApi.checkoutBooking(token, item.getId()))
                        .toList()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(results -> {
                            setLoading(false);
                            if (!results.isEmpty()) {
                                // Lấy ID của booking đầu tiên vừa checkout thành công
                                Long firstBookingId = results.get(0).getData().getId();
                                
                                // Chuyển sang màn hình xác nhận thanh toán (Reuse luồng Book Now)
                                Intent intent = new Intent(getContext(), ConfirmPaymentActivity.class);
                                intent.putExtra(ConfirmPaymentActivity.EXTRA_BOOKING_ID, firstBookingId);
                                startActivity(intent);
                            } else {
                                fetchCart();
                            }
                        }, error -> {
                            setLoading(false);
                            showMessage("Checkout failed: " + getErrorMessage(error));
                        })
        );
    }

    private String getErrorMessage(Throwable error) {
        if (error instanceof HttpException) {
            try {
                HttpException httpException = (HttpException) error;
                if (httpException.response() != null && httpException.response().errorBody() != null) {
                    String errorBody = httpException.response().errorBody().string();
                    ApiResponse<?> apiResponse = new Gson().fromJson(errorBody, ApiResponse.class);
                    if (apiResponse != null) {
                        if (apiResponse.getError() != null && !apiResponse.getError().isEmpty()) {
                            return apiResponse.getError();
                        }
                        if (apiResponse.getMessage() != null && !apiResponse.getMessage().isEmpty()) {
                            return apiResponse.getMessage();
                        }
                    }
                }
            } catch (Exception e) {
                // Ignore parsing errors
            }
        }
        return error.getMessage();
    }

    private void setLoading(boolean loading) {
        binding.btnCheckout.setEnabled(!loading);
        // binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    private void showMessage(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        disposables.clear();
        binding = null;
    }
}
