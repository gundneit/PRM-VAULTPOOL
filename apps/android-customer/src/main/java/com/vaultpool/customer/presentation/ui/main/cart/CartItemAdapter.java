package com.vaultpool.customer.presentation.ui.main.cart;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.vaultpool.customer.databinding.ItemCartBinding;
import com.vaultpool.customer.data.remote.dto.BookingResponseDto;

import java.util.List;

public class CartItemAdapter extends RecyclerView.Adapter<CartItemAdapter.ViewHolder> {

    private List<BookingResponseDto> items;
    private final OnItemSelectionChangeListener listener;
    private int selectedPosition = -1;

    public interface OnItemSelectionChangeListener {
        void onSelectionChanged();
    }

    public CartItemAdapter(List<BookingResponseDto> items, OnItemSelectionChangeListener listener) {
        this.items = items;
        this.listener = listener;
    }

    public void updateItems(List<BookingResponseDto> newItems) {
        this.items = newItems;
        this.selectedPosition = -1;
        notifyDataSetChanged();
    }

    public List<BookingResponseDto> getItems() {
        return items;
    }

    public BookingResponseDto getSelectedItem() {
        if (selectedPosition != -1 && selectedPosition < items.size()) {
            return items.get(selectedPosition);
        }
        return null;
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
        holder.bind(item, position);
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

        void bind(BookingResponseDto booking, int position) {
            binding.tvPoolName.setText(booking.getPoolName());
            binding.tvPoolAddress.setText(booking.getPoolAddress());
            binding.tvQuantity.setText("Guests: " + booking.getQty());
            binding.tvAmount.setText(String.format("₫ %,d", booking.getAmount() != null ? booking.getAmount() : 0));
            binding.tvBookingCode.setText("Code: " + (booking.getBookingCode() != null ? booking.getBookingCode() : "N/A"));

            // Logic check availability in Cart
            int qtyNeeded = booking.getQty() != null ? booking.getQty() : 1;
            int available = booking.getCapacityAvailable() != null ? booking.getCapacityAvailable() : 999;
            
            boolean isFull = available <= 0;
            boolean notEnoughSpace = qtyNeeded > available;
            boolean isDisabled = isFull || notEnoughSpace;

            if (isDisabled) {
                binding.tvError.setVisibility(View.VISIBLE);
                binding.tvError.setText(isFull ? "Hết chỗ trống" : "Không đủ chỗ (Còn " + available + ")");
                binding.cbSelect.setEnabled(false);
                binding.layoutContent.setAlpha(0.5f);
                binding.cbSelect.setChecked(false);
                if (selectedPosition == position) selectedPosition = -1;
            } else {
                binding.tvError.setVisibility(View.GONE);
                binding.cbSelect.setEnabled(true);
                binding.layoutContent.setAlpha(1.0f);
                binding.cbSelect.setChecked(position == selectedPosition);
            }

            binding.cbSelect.setOnClickListener(v -> {
                if (isDisabled) return;
                
                int oldPosition = selectedPosition;
                if (selectedPosition == position) {
                    selectedPosition = -1;
                } else {
                    selectedPosition = position;
                }
                
                if (oldPosition != -1) notifyItemChanged(oldPosition);
                notifyItemChanged(selectedPosition);
                
                if (listener != null) {
                    listener.onSelectionChanged();
                }
            });

            itemView.setOnClickListener(v -> {
                if (!isDisabled) {
                    binding.cbSelect.performClick();
                }
            });
        }
    }
}
