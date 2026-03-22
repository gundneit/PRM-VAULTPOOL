package com.vaultpool.customer.presentation.ui.staff;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.vaultpool.customer.data.remote.dto.staff.PoolStaffDto;
import com.vaultpool.customer.databinding.ItemPoolStaffBinding;

import java.util.ArrayList;
import java.util.List;

public class StaffPoolAdapter extends RecyclerView.Adapter<StaffPoolAdapter.ViewHolder> {

    private List<PoolStaffDto> pools = new ArrayList<>();
    private OnPoolClickListener listener;

    public interface OnPoolClickListener {
        void onPoolClick(PoolStaffDto pool);
        void onStatusToggle(PoolStaffDto pool);
    }

    public void setOnPoolClickListener(OnPoolClickListener listener) {
        this.listener = listener;
    }

    public void setPools(List<PoolStaffDto> pools) {
        this.pools = pools;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemPoolStaffBinding binding = ItemPoolStaffBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(pools.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return pools.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemPoolStaffBinding binding;

        public ViewHolder(ItemPoolStaffBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(PoolStaffDto pool, OnPoolClickListener listener) {
            binding.tvPoolName.setText(pool.getName());
            binding.tvPoolAddress.setText(pool.getAddress());
            binding.tvPoolStatus.setText(pool.getStatus());
            binding.tvOpenHours.setText(pool.getOpenHours());
            
            int colorResId = "ACTIVE".equalsIgnoreCase(pool.getStatus()) 
                    ? com.vaultpool.customer.R.color.success 
                    : com.vaultpool.customer.R.color.error;
            
            binding.tvPoolStatus.setBackgroundColor(ContextCompat.getColor(binding.getRoot().getContext(), colorResId));
            
            // Cập nhật trạng thái khi nhấn vào nhãn status
            binding.tvPoolStatus.setOnClickListener(v -> {
                if (listener != null) listener.onStatusToggle(pool);
            });

            // Mở chỉnh sửa khi nhấn vào toàn bộ item
            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) listener.onPoolClick(pool);
            });
        }
    }
}
