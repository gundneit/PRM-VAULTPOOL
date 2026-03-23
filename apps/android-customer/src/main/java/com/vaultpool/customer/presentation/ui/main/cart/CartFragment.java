package com.vaultpool.customer.presentation.ui.main.cart;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.vaultpool.customer.ServiceLocator;
import com.vaultpool.customer.databinding.FragmentCartBinding;
import com.vaultpool.customer.data.local.prefs.PreferencesManager;
import com.vaultpool.customer.data.remote.api.BookingApi;
import com.vaultpool.customer.data.remote.dto.BookingResponseDto;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class CartFragment extends Fragment {

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
        fetchCart();
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
        adapter = new CartItemAdapter(new ArrayList<>());
        binding.rvCart.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvCart.setAdapter(adapter);
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
                            showMessage("Error loading cart: " + error.getMessage());
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

            long total = 0;
            for (BookingResponseDto item : items) {
                total += item.getAmount() != null ? item.getAmount() : 0;
            }
            binding.tvTotalAmount.setText(String.format("₫ %,d", total));
        }
    }

    private void setupCheckoutButton() {
        binding.btnCheckout.setOnClickListener(v -> {
            performCheckout();
        });
    }

    private void performCheckout() {
        List<BookingResponseDto> items = adapter.getItems();
        if (items == null || items.isEmpty()) {
            showMessage("Cart is empty");
            return;
        }

        String firebaseToken = preferencesManager.getFirebaseToken();
        if (firebaseToken == null) return;
        String token = firebaseToken.startsWith("Bearer ") ? firebaseToken : "Bearer " + firebaseToken;

        setLoading(true);
        // Checkout all items in the cart
        disposables.add(
                Observable.fromIterable(items)
                        .flatMapSingle(item -> bookingApi.checkoutBooking(token, item.getId()))
                        .toList()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(results -> {
                            setLoading(false);
                            showMessage("Checkout successful!");
                            fetchCart(); // Refresh cart
                        }, error -> {
                            setLoading(false);
                            showMessage("Checkout failed: " + error.getMessage());
                        })
        );
    }

    private void setLoading(boolean loading) {
        binding.btnCheckout.setEnabled(!loading);
        // If you have a progress bar in layout, toggle it here
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
