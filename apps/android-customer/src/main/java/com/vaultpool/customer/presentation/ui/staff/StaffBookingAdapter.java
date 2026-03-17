package com.vaultpool.customer.presentation.ui.staff;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.vaultpool.customer.data.remote.dto.staff.BookingStaffDto;
import com.vaultpool.customer.databinding.ItemBookingStaffBinding;

import java.util.ArrayList;
import java.util.List;

public class StaffBookingAdapter extends RecyclerView.Adapter<StaffBookingAdapter.ViewHolder> {

    private List<BookingStaffDto> bookings = new ArrayList<>();

    public void setBookings(List<BookingStaffDto> bookings) {
        this.bookings = bookings;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemBookingStaffBinding binding = ItemBookingStaffBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(bookings.get(position));
    }

    @Override
    public int getItemCount() {
        return bookings.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemBookingStaffBinding binding;

        public ViewHolder(ItemBookingStaffBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(BookingStaffDto booking) {
            binding.tvBookingCode.setText(booking.getBookingCode());
            binding.tvPoolName.setText(booking.getPoolName());
            
            String startTime = booking.getStartTime() != null && booking.getStartTime().length() >= 16 
                    ? booking.getStartTime().substring(11, 16) : "N/A";
            String endTime = booking.getEndTime() != null && booking.getEndTime().length() >= 16 
                    ? booking.getEndTime().substring(11, 16) : "N/A";
            
            binding.tvTime.setText(startTime + " - " + endTime);
            binding.tvQty.setText(booking.getQty() + " Tickets");
            binding.tvAmount.setText(String.format("%,d", booking.getAmount()));
            binding.tvStatus.setText(booking.getStatus());
            
            int colorResId;
            switch (booking.getStatus()) {
                case "COMPLETED":
                case "SUCCESS":
                    colorResId = com.vaultpool.customer.R.color.success;
                    break;
                case "CANCELED":
                case "EXPIRED":
                    colorResId = com.vaultpool.customer.R.color.error;
                    break;
                default:
                    colorResId = com.vaultpool.customer.R.color.warning;
                    break;
            }
            binding.tvStatus.setBackgroundColor(ContextCompat.getColor(binding.getRoot().getContext(), colorResId));
        }
    }
}
