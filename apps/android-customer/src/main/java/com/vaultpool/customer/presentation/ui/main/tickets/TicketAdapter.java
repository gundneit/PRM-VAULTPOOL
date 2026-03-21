package com.vaultpool.customer.presentation.ui.main.tickets;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.vaultpool.customer.databinding.ItemTicketBinding;
import com.vaultpool.customer.domain.model.Ticket;
import java.util.List;

public class TicketAdapter extends RecyclerView.Adapter<TicketAdapter.ViewHolder> {
    private final List<Ticket> tickets;

    public TicketAdapter(List<Ticket> tickets) {
        this.tickets = tickets;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemTicketBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Ticket ticket = tickets.get(position);
        holder.binding.tvTicketTitle.setText(ticket.getTitle());
        holder.binding.tvDescription.setText(ticket.getDescription());
        holder.binding.tvPrice.setText(ticket.getPrice());
        holder.binding.tvFlashSale.setText(ticket.getFlashSaleText());
    }

    @Override
    public int getItemCount() { return tickets.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemTicketBinding binding;
        ViewHolder(ItemTicketBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
