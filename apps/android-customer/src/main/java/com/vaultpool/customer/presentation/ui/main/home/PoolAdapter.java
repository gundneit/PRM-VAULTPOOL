package com.vaultpool.customer.presentation.ui.main.home;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.vaultpool.customer.databinding.ItemPoolBinding;
import com.vaultpool.customer.domain.model.Pool;
import java.util.List;

public class PoolAdapter extends RecyclerView.Adapter<PoolAdapter.ViewHolder> {
    private final List<Pool> pools;

    public PoolAdapter(List<Pool> pools) {
        this.pools = pools;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemPoolBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Pool pool = pools.get(position);
        holder.binding.poolName.setText(pool.getName());
        holder.binding.poolLocation.setText(pool.getLocation());
        holder.binding.poolPrice.setText(pool.getPrice());
        holder.binding.poolRating.setText(String.valueOf(pool.getRating()));
        // For now, we use a placeholder or the drawable from the model if we had one.
        // holder.binding.poolImage.setImageResource(...);
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
