package com.vaultpool.customer.presentation.ui.staff;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.vaultpool.customer.data.remote.dto.staff.ImageStaffDto;
import com.vaultpool.customer.databinding.ItemPoolImageEditBinding;

import java.util.ArrayList;
import java.util.List;

public class StaffPoolImageAdapter extends RecyclerView.Adapter<StaffPoolImageAdapter.ViewHolder> {

    private final List<ImageStaffDto> images = new ArrayList<>();
    private OnImageRemoveListener onImageRemoveListener;

    public interface OnImageRemoveListener {
        void onRemove(int position);
    }

    public void setOnImageRemoveListener(OnImageRemoveListener listener) {
        this.onImageRemoveListener = listener;
    }

    public void setImages(List<ImageStaffDto> newImages) {
        images.clear();
        if (newImages != null) {
            images.addAll(newImages);
        }
        notifyDataSetChanged();
    }

    public void addImage(ImageStaffDto image) {
        images.add(image);
        notifyItemInserted(images.size() - 1);
    }

    public List<ImageStaffDto> getImages() {
        return images;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemPoolImageEditBinding binding = ItemPoolImageEditBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(images.get(position));
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemPoolImageEditBinding binding;

        ViewHolder(ItemPoolImageEditBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(ImageStaffDto image) {
            binding.tvImageUrl.setText(image.getImageUrl());
            
            Glide.with(binding.ivPoolImage.getContext())
                    .load(image.getImageUrl())
                    .placeholder(com.vaultpool.customer.R.drawable.logo)
                    .error(com.vaultpool.customer.R.drawable.logo)
                    .into(binding.ivPoolImage);

            binding.btnRemoveImage.setOnClickListener(v -> {
                if (onImageRemoveListener != null) {
                    onImageRemoveListener.onRemove(getAdapterPosition());
                }
            });
        }
    }
}
