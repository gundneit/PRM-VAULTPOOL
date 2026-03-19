package com.vaultpool.customer.presentation.ui.main.home;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.vaultpool.customer.databinding.ItemDateSelectionBinding;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class DateAdapter extends RecyclerView.Adapter<DateAdapter.ViewHolder> {
    private final List<Calendar> dates;
    private int selectedPosition = 0;
    private final OnDateClickListener listener;
    private final SimpleDateFormat dayFormat = new SimpleDateFormat("EEE", Locale.ENGLISH);
    private final SimpleDateFormat dayNumFormat = new SimpleDateFormat("dd", Locale.ENGLISH);

    public interface OnDateClickListener {
        void onDateClick(Calendar date);
    }

    public DateAdapter(List<Calendar> dates, OnDateClickListener listener) {
        this.dates = dates;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemDateSelectionBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Calendar date = dates.get(position);
        holder.binding.tvDayName.setText(dayFormat.format(date.getTime()));
        holder.binding.tvDayNumber.setText(dayNumFormat.format(date.getTime()));

        if (position == selectedPosition) {
            holder.binding.cardDate.setCardBackgroundColor(Color.parseColor("#0077B6"));
            holder.binding.tvDayName.setTextColor(Color.WHITE);
            holder.binding.tvDayNumber.setTextColor(Color.WHITE);
            holder.binding.cardDate.setStrokeWidth(0);
        } else {
            holder.binding.cardDate.setCardBackgroundColor(Color.WHITE);
            holder.binding.tvDayName.setTextColor(Color.parseColor("#9E9E9E"));
            holder.binding.tvDayNumber.setTextColor(Color.parseColor("#212121"));
            holder.binding.cardDate.setStrokeWidth(1);
        }

        holder.itemView.setOnClickListener(v -> {
            int oldPos = selectedPosition;
            selectedPosition = holder.getAdapterPosition();
            notifyItemChanged(oldPos);
            notifyItemChanged(selectedPosition);
            listener.onDateClick(date);
        });
    }

    @Override
    public int getItemCount() { return dates.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemDateSelectionBinding binding;
        ViewHolder(ItemDateSelectionBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
