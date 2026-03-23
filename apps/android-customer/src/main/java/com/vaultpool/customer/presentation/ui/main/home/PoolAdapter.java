package com.vaultpool.customer.presentation.ui.main.home;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.vaultpool.customer.databinding.ItemPoolBinding;
import com.vaultpool.customer.data.remote.dto.PoolDto;
import java.util.ArrayList;
import java.util.List;

public class PoolAdapter extends RecyclerView.Adapter<PoolAdapter.ViewHolder> {
    private List<PoolDto> pools;

    public PoolAdapter(List<PoolDto> pools) {
        this.pools = new ArrayList<>(pools);
    }

    public void updateData(List<PoolDto> newPools) {
        this.pools = new ArrayList<>(newPools);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemPoolBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PoolDto pool = pools.get(position);
        holder.binding.poolName.setText(pool.getName());
        holder.binding.poolLocation.setText(pool.getAddress());
        holder.binding.poolPrice.setText("ACTIVE".equals(pool.getStatus()) ? "Open" : "Closed");
        holder.binding.poolRating.setText("4.9");

        if (pool.getImages() != null && !pool.getImages().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(pool.getImages().get(0).getImageUrl())
                    .into(holder.binding.poolImage);
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(holder.itemView.getContext(), PoolDetailActivity.class);
            intent.putExtra(PoolDetailActivity.EXTRA_POOL_ID, pool.getId());
            holder.itemView.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() { return pools.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemPoolBinding binding;
        ViewHolder(ItemPoolBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
