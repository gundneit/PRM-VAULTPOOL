package com.vaultpool.customer.presentation.ui.staff;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.vaultpool.customer.data.remote.dto.staff.RevenueAnalyticsDto;
import com.vaultpool.customer.databinding.ItemStaffRevenueTimeBinding;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class StaffRevenueTimeAdapter extends RecyclerView.Adapter<StaffRevenueTimeAdapter.ViewHolder> {

    private final List<RevenueAnalyticsDto> items = new ArrayList<>();
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

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
        ItemStaffRevenueTimeBinding binding = ItemStaffRevenueTimeBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(items.get(position), currencyFormat);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private final ItemStaffRevenueTimeBinding binding;

        ViewHolder(ItemStaffRevenueTimeBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(RevenueAnalyticsDto item, NumberFormat currencyFormat) {
            binding.tvPeriod.setText(valueOrDash(item.getPeriod()));

            long revenue = item.getTotalRevenue() == null ? 0L : item.getTotalRevenue();
            binding.tvRevenue.setText(currencyFormat.format(revenue));

            int tickets = item.getTicketsSold() == null ? 0 : item.getTicketsSold();
            int bookings = item.getBookingCount() == null ? 0 : item.getBookingCount();
            binding.tvTickets.setText(tickets + " tickets");
            binding.tvBookings.setText(bookings + " bookings");
        }

        private String valueOrDash(String value) {
            return value == null || value.trim().isEmpty() ? "-" : value;
        }
    }
}
