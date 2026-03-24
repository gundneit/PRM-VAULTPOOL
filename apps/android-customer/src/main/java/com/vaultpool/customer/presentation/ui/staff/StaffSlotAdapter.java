package com.vaultpool.customer.presentation.ui.staff;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.vaultpool.customer.data.remote.dto.staff.SlotStaffDto;
import com.vaultpool.customer.databinding.ItemSlotStaffBinding;

import java.util.ArrayList;
import java.util.List;

public class StaffSlotAdapter extends RecyclerView.Adapter<StaffSlotAdapter.ViewHolder> {

    private List<SlotStaffDto> slots = new ArrayList<>();
    private OnSlotClickListener listener;

    public interface OnSlotClickListener {
        void onStatusToggle(SlotStaffDto slot);
        void onSlotClick(SlotStaffDto slot);
    }

    public void setOnSlotClickListener(OnSlotClickListener listener) {
        this.listener = listener;
    }

    public void setSlots(List<SlotStaffDto> slots) {
        this.slots = slots;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemSlotStaffBinding binding = ItemSlotStaffBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(slots.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return slots.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemSlotStaffBinding binding;

        public ViewHolder(ItemSlotStaffBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(SlotStaffDto slot, OnSlotClickListener listener) {
            try {
                String start = slot.getStartTime().length() >= 16 ? slot.getStartTime().substring(11, 16) : slot.getStartTime();
                String end = slot.getEndTime().length() >= 16 ? slot.getEndTime().substring(11, 16) : slot.getEndTime();
                binding.tvTime.setText(start + " - " + end);
            } catch (Exception e) {
                binding.tvTime.setText(slot.getStartTime() + " - " + slot.getEndTime());
            }
            
            binding.tvPrice.setText(String.format("%,d VND", slot.getPrice()));
                int total = slot.getCapacityTotal() != null ? slot.getCapacityTotal() : 0;
                int available = slot.getCapacityAvailable() != null ? slot.getCapacityAvailable() : 0;
                int used = Math.max(0, total - available);
                binding.tvCapacity.setText(String.format("Capacity: %d/%d", used, total));
            
            binding.tvStatus.setText(slot.getStatus());
            
            int colorResId;
            if ("ACTIVE".equalsIgnoreCase(slot.getStatus())) {
                colorResId = com.vaultpool.customer.R.color.success;
            } else if ("EXPIRED".equalsIgnoreCase(slot.getStatus())) {
                colorResId = com.vaultpool.customer.R.color.text_hint;
            } else {
                colorResId = com.vaultpool.customer.R.color.error;
            }
            
            binding.tvStatus.setBackgroundTintList(ContextCompat.getColorStateList(binding.getRoot().getContext(), colorResId));
            
            // Status Switch logic
            binding.switchStatus.setOnCheckedChangeListener(null);
            binding.switchStatus.setChecked("ACTIVE".equalsIgnoreCase(slot.getStatus()));
            
            boolean isExpired = "EXPIRED".equalsIgnoreCase(slot.getStatus());
            binding.switchStatus.setEnabled(!isExpired);
            binding.switchStatus.setAlpha(isExpired ? 0.5f : 1.0f);

            binding.switchStatus.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (listener != null) {
                    listener.onStatusToggle(slot);
                }
            });

            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) listener.onSlotClick(slot);
            });
        }
    }
}