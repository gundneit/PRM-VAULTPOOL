package com.vaultpool.customer.presentation.ui.main.cart;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.vaultpool.customer.databinding.ItemCartBinding;
import com.vaultpool.customer.data.remote.dto.BookingResponseDto;

import java.util.List;

public class CartItemAdapter extends RecyclerView.Adapter<CartItemAdapter.ViewHolder> {

    private List<BookingResponseDto> items;
    private final OnItemSelectionChangeListener listener;

    public interface OnItemSelectionChangeListener {
        void onSelectionChanged();
    }

    public CartItemAdapter(List<BookingResponseDto> items, OnItemSelectionChangeListener listener) {
        this.items = items;
        this.listener = listener;
    }

    public void updateItems(List<BookingResponseDto> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    public List<BookingResponseDto> getItems() {
        return items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemCartBinding.inflate(
                LayoutInflater.from(parent.getContext()), 
                parent, 
                false
        ));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BookingResponseDto item = items.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemCartBinding binding;

        ViewHolder(ItemCartBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(BookingResponseDto booking) {
            binding.tvPoolName.setText(booking.getPoolName());
            binding.tvPoolAddress.setText(booking.getPoolAddress());
            binding.tvQuantity.setText("Quantity: " + booking.getQty());
            binding.tvAmount.setText(String.format("₫ %,d", booking.getAmount() != null ? booking.getAmount() : 0));
            binding.tvBookingCode.setText("Code: " + booking.getBookingCode());

            // Handle selection
            binding.cbSelect.setOnCheckedChangeListener(null); // Clear listener to avoid recursive calls
            binding.cbSelect.setChecked(booking.isSelected());
            binding.cbSelect.setOnCheckedChangeListener((buttonView, isChecked) -> {
                booking.setSelected(isChecked);
                if (listener != null) {
                    listener.onSelectionChanged();
                }
            });
        }
    }
}
