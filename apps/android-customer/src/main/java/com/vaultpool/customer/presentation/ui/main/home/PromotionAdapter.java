package com.vaultpool.customer.presentation.ui.main.home;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.vaultpool.customer.databinding.ItemPromotionBinding;
import com.vaultpool.customer.domain.model.Promotion;
import java.util.List;

public class PromotionAdapter extends RecyclerView.Adapter<PromotionAdapter.ViewHolder> {
    private final List<Promotion> promotions;

    public PromotionAdapter(List<Promotion> promotions) {
        this.promotions = promotions;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemPromotionBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Promotion promo = promotions.get(position);
        holder.binding.tvPromoTitle.setText(promo.getTitle());
    }

    @Override
    public int getItemCount() { return promotions.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemPromotionBinding binding;
        ViewHolder(ItemPromotionBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
