package com.vaultpool.customer.presentation.ui.staff;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.vaultpool.customer.data.remote.dto.staff.RevenueAnalyticsDto;
import com.vaultpool.customer.databinding.ItemStaffRevenuePoolBinding;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class StaffRevenuePoolAdapter extends RecyclerView.Adapter<StaffRevenuePoolAdapter.ViewHolder> {

    private final List<RevenueAnalyticsDto> items = new ArrayList<>();
    private final NumberFormat numberFormat = NumberFormat.getNumberInstance(new Locale("vi", "VN"));

    public void setItems(List<RevenueAnalyticsDto> data) {
        items.clear();
        if (data != null) {
            items.addAll(data);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemStaffRevenuePoolBinding binding = ItemStaffRevenuePoolBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(items.get(position), position + 1, numberFormat);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private final ItemStaffRevenuePoolBinding binding;

        ViewHolder(ItemStaffRevenuePoolBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(RevenueAnalyticsDto item, int rank, NumberFormat numberFormat) {
            binding.tvRank.setText(String.valueOf(rank));

            String poolName = firstNonEmpty(item.getPoolName(), item.getPeriod(), "Unknown pool");
            binding.tvPoolName.setText(poolName);

            int tickets = item.getTicketsSold() == null ? 0 : item.getTicketsSold();
            int bookings = item.getBookingCount() == null ? 0 : item.getBookingCount();
            binding.tvPoolMeta.setText(tickets + " tickets - " + bookings + " bookings");

            long revenue = item.getTotalRevenue() == null ? 0L : item.getTotalRevenue();
            binding.tvPoolRevenue.setText(numberFormat.format(revenue) + " VND");
        }

        private String firstNonEmpty(String primary, String fallback, String defaultValue) {
            if (primary != null && !primary.trim().isEmpty()) {
                return primary;
            }
            if (fallback != null && !fallback.trim().isEmpty()) {
                return fallback;
            }
            return defaultValue;
        }
    }
}
