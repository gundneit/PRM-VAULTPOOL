package com.vaultpool.customer.presentation.ui.staff;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.vaultpool.customer.R;
import com.vaultpool.customer.data.remote.dto.staff.InventoryLogDto;
import com.vaultpool.customer.databinding.ItemStaffInventoryLogBinding;

import java.util.ArrayList;
import java.util.List;

public class StaffInventoryLogAdapter extends RecyclerView.Adapter<StaffInventoryLogAdapter.ViewHolder> {

    private final List<InventoryLogDto> items = new ArrayList<>();

    public void setItems(List<InventoryLogDto> data) {
        items.clear();
        if (data != null) {
            items.addAll(data);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemStaffInventoryLogBinding binding = ItemStaffInventoryLogBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private final ItemStaffInventoryLogBinding binding;

        ViewHolder(ItemStaffInventoryLogBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(InventoryLogDto item) {
            binding.tvReason.setText(valueOrDash(item.getReason()));

            int delta = item.getDelta() == null ? 0 : item.getDelta();
            String deltaText = delta > 0 ? "+" + delta : String.valueOf(delta);
            binding.tvDelta.setText(deltaText);
            binding.tvDelta.setTextColor(ContextCompat.getColor(binding.getRoot().getContext(), getDeltaColor(delta)));

            binding.tvBookingCode.setText(firstNonEmpty(item.getBookingCode(), "No booking code"));

                String meta = "Slot #" + valueOrDash(numberToString(item.getSlotId()))
                    + " - capAfter: " + valueOrDash(numberToString(item.getCapacityAfter()))
                    + " - " + firstNonEmpty(item.getActor(), "-");
            binding.tvMeta.setText(meta);

            binding.tvCreatedAt.setText(firstNonEmpty(item.getCreatedAt(), "-"));

            binding.tvReason.setBackgroundTintList(
                    ContextCompat.getColorStateList(binding.getRoot().getContext(), getReasonColor(item.getReason()))
            );
        }

        private int getDeltaColor(int delta) {
            if (delta > 0) {
                return R.color.success;
            }
            if (delta < 0) {
                return R.color.error;
            }
            return R.color.text_secondary;
        }

        private int getReasonColor(String reason) {
            if (reason == null) {
                return R.color.info;
            }
            String normalized = reason.trim().toUpperCase();
            switch (normalized) {
                case "CONFIRM":
                    return R.color.success;
                case "RESERVE":
                    return R.color.warning;
                case "EXPIRE":
                case "CANCEL":
                case "RELEASE":
                    return R.color.error;
                default:
                    return R.color.info;
            }
        }

        private String numberToString(Number value) {
            return value == null ? null : String.valueOf(value.longValue());
        }

        private String valueOrDash(String value) {
            return value == null || value.trim().isEmpty() ? "-" : value;
        }

        private String firstNonEmpty(String value, String fallback) {
            if (value != null && !value.trim().isEmpty()) {
                return value;
            }
            return fallback;
        }
    }
}
