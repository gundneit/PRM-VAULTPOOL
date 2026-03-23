package com.vaultpool.customer.presentation.ui.main.bookings;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.vaultpool.customer.R;
import com.vaultpool.customer.data.remote.dto.BookingResponseDto;
import com.vaultpool.customer.databinding.ItemBookingBinding;

import java.util.List;

public class BookingItemAdapter extends RecyclerView.Adapter<BookingItemAdapter.ViewHolder> {

    public interface OnBookingClickListener {
        void onClick(BookingResponseDto booking);
    }

    private List<BookingResponseDto> items;
    private final OnBookingClickListener listener;

    public BookingItemAdapter(List<BookingResponseDto> items, OnBookingClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    public void updateItems(List<BookingResponseDto> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemBookingBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(items.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemBookingBinding binding;

        ViewHolder(ItemBookingBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(BookingResponseDto b, OnBookingClickListener listener) {
            binding.tvPoolName.setText(b.getPoolName());
            binding.tvPoolAddress.setText(b.getPoolAddress());
            binding.tvBookingCode.setText("Code: " + b.getBookingCode());
            binding.tvQty.setText(b.getQty() + " ticket(s)");
            binding.tvAmount.setText(String.format("₫ %,d", b.getAmount() != null ? b.getAmount() : 0));

            String start = formatTime(b.getStartTime());
            String end = formatTime(b.getEndTime());
            binding.tvTime.setText(start + " – " + end);

            String status = b.getStatus() != null ? b.getStatus() : "";
            binding.tvStatus.setText(status);
            
            int colorRes;
            switch (status) {
                case "CONFIRMED":       colorRes = R.color.success; break;
                case "CHECKED_IN":      colorRes = R.color.info;    break;
                case "PENDING_PAYMENT": colorRes = R.color.warning; break;
                case "CANCELED":
                case "EXPIRED":
                case "FAILED":          colorRes = R.color.error;   break;
                default:                colorRes = R.color.text_hint; break;
            }
            binding.tvStatus.setBackgroundTintList(
                    ContextCompat.getColorStateList(binding.getRoot().getContext(), colorRes));

            // QR Code Logic for CONFIRMED status
            if ("CONFIRMED".equals(status)) {
                binding.layoutQrCode.setVisibility(View.VISIBLE);
                binding.tvBookingCode.setVisibility(View.GONE); // Hide small code when QR is shown
                binding.tvBookingCodeLarge.setText(b.getBookingCode());
                try {
                    BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                    Bitmap bitmap = barcodeEncoder.encodeBitmap(b.getBookingCode(), BarcodeFormat.QR_CODE, 400, 400);
                    binding.ivQrCode.setImageBitmap(bitmap);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                binding.layoutQrCode.setVisibility(View.GONE);
                binding.tvBookingCode.setVisibility(View.VISIBLE);
            }

            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) listener.onClick(b);
            });

            if ("PENDING_PAYMENT".equals(status)) {
                binding.getRoot().setStrokeColor(
                        ContextCompat.getColor(binding.getRoot().getContext(), R.color.warning));
                binding.getRoot().setStrokeWidth(2);
                binding.tvPendingHint.setVisibility(View.VISIBLE);
            } else {
                binding.getRoot().setStrokeColor(
                        ContextCompat.getColor(binding.getRoot().getContext(), R.color.divider));
                binding.getRoot().setStrokeWidth(1);
                binding.tvPendingHint.setVisibility(View.GONE);
            }
        }

        private String formatTime(String iso) {
            if (iso == null || iso.length() < 16) return iso != null ? iso : "-";
            String time = iso.substring(11, 16);
            String date = iso.substring(5, 10).replace("-", "/");
            return time + " · " + date;
        }
    }
}
