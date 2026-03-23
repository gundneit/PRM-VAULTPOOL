package com.vaultpool.customer.presentation.ui.main.home;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.vaultpool.customer.data.remote.dto.SlotDto;
import com.vaultpool.customer.databinding.ItemSlotTimeBinding;

import java.time.LocalDateTime;
import java.util.List;

public class TimeSlotAdapter extends RecyclerView.Adapter<TimeSlotAdapter.ViewHolder> {
    private final List<SlotDto> slots;
    private int selectedPosition = -1;
    private final OnSlotClickListener listener;

    public interface OnSlotClickListener {
        void onSlotClick(SlotDto slot, TimeSlotAdapter adapter);
    }

    public TimeSlotAdapter(List<SlotDto> slots, OnSlotClickListener listener) {
        this.slots = slots;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemSlotTimeBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SlotDto slot = slots.get(position);
        holder.binding.tvTime.setText(formatTime(slot.getStartTime()));

        // Logic check expired or unavailable
        boolean isExpiredByStatus = "EXPIRED".equalsIgnoreCase(slot.getStatus());
        boolean isFull = slot.getCapacityAvailable() != null && slot.getCapacityAvailable() <= 0;
        
        boolean isExpiredByTime = false;
        try {
            if (slot.getStartTime() != null) {
                LocalDateTime startTime = LocalDateTime.parse(slot.getStartTime());
                if (startTime.isBefore(LocalDateTime.now())) {
                    isExpiredByTime = true;
                }
            }
        } catch (Exception ignored) {}

        // Nếu bất kỳ điều kiện nào thỏa mãn thì đều Disable
        final boolean isDisabled = isExpiredByStatus || isExpiredByTime || isFull;

        if (isDisabled) {
            holder.binding.cardTime.setCardBackgroundColor(Color.parseColor("#F5F5F5"));
            holder.binding.tvTime.setTextColor(Color.parseColor("#BDBDBD"));
            holder.binding.cardTime.setStrokeWidth(0);
            holder.binding.cardTime.setAlpha(0.5f);
            
            // Nếu slot bị disable mà lỡ đang được chọn (do logic cũ), thì bỏ chọn nó
            if (selectedPosition == position) {
                selectedPosition = -1;
            }
        } else if (position == selectedPosition) {
            holder.binding.cardTime.setCardBackgroundColor(Color.parseColor("#0077B6"));
            holder.binding.tvTime.setTextColor(Color.WHITE);
            holder.binding.cardTime.setStrokeWidth(0);
            holder.binding.cardTime.setAlpha(1.0f);
        } else {
            holder.binding.cardTime.setCardBackgroundColor(Color.WHITE);
            holder.binding.tvTime.setTextColor(Color.parseColor("#212121"));
            holder.binding.cardTime.setStrokeWidth(1);
            holder.binding.cardTime.setAlpha(1.0f);
        }

        holder.itemView.setOnClickListener(v -> {
            if (isDisabled) return; // Tuyệt đối không cho click nếu disable
            
            if (selectedPosition == holder.getAdapterPosition()) return;
            
            int oldPos = selectedPosition;
            selectedPosition = holder.getAdapterPosition();
            notifyItemChanged(oldPos);
            notifyItemChanged(selectedPosition);
            listener.onSlotClick(slot, this);
        });
    }

    public void clearSelection() {
        int oldPos = selectedPosition;
        selectedPosition = -1;
        if (oldPos != -1) {
            notifyItemChanged(oldPos);
        }
    }

    public int getSelectedPosition() {
        return selectedPosition;
    }

    private String formatTime(String isoTime) {
        if (isoTime == null || isoTime.length() < 16) return isoTime;
        return isoTime.substring(11, 16);
    }

    @Override
    public int getItemCount() { return slots.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemSlotTimeBinding binding;
        ViewHolder(ItemSlotTimeBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
