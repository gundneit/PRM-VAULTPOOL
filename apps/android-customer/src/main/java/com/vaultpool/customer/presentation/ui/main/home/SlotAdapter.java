package com.vaultpool.customer.presentation.ui.main.home;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.vaultpool.customer.data.remote.dto.SlotDto;
import com.vaultpool.customer.databinding.ItemSlotBinding;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class SlotAdapter extends RecyclerView.Adapter<SlotAdapter.ViewHolder> {
    private final List<SlotDto> slots;
    private final OnSlotClickListener listener;

    public interface OnSlotClickListener {
        void onBookClick(SlotDto slot);
    }

    public SlotAdapter(List<SlotDto> slots, OnSlotClickListener listener) {
        this.slots = slots;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemSlotBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SlotDto slot = slots.get(position);
        
        String timeRange = formatTime(slot.getStartTime()) + " - " + formatTime(slot.getEndTime());
        holder.binding.tvTime.setText(timeRange);
        
        holder.binding.tvCapacity.setText(slot.getCapacityAvailable() + " spots left");
        
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        holder.binding.tvSlotPrice.setText(formatter.format(slot.getPrice()));
        
        // Logic check expired
        boolean isExpired = false;
        try {
            if (slot.getStartTime() != null) {
                LocalDateTime startTime = LocalDateTime.parse(slot.getStartTime());
                if (startTime.isBefore(LocalDateTime.now())) {
                    isExpired = true;
                }
            }
        } catch (Exception e) {
            isExpired = "EXPIRED".equals(slot.getStatus());
        }

        boolean canBook = slot.getCapacityAvailable() > 0 && !isExpired && !"EXPIRED".equals(slot.getStatus());
        
        holder.binding.btnBookNow.setEnabled(canBook);
        
        if (!canBook) {
            holder.binding.btnBookNow.setText(isExpired ? "Expired" : "Full");
            holder.binding.getRoot().setAlpha(0.6f); // Làm mờ item
        } else {
            holder.binding.btnBookNow.setText("Book Now");
            holder.binding.getRoot().setAlpha(1.0f);
        }

        holder.binding.btnBookNow.setOnClickListener(v -> {
            if (canBook) {
                listener.onBookClick(slot);
            }
        });
    }

    private String formatTime(String isoTime) {
        if (isoTime == null || isoTime.length() < 16) return isoTime;
        return isoTime.substring(11, 16); // Lấy HH:mm từ yyyy-MM-ddTHH:mm:ss
    }

    @Override
    public int getItemCount() { return slots.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemSlotBinding binding;
        ViewHolder(ItemSlotBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
