package com.vaultpool.customer.presentation.ui.main.tickets;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.vaultpool.customer.databinding.FragmentTicketsBinding;
import com.vaultpool.customer.domain.model.Ticket;
import java.util.ArrayList;
import java.util.List;

public class TicketsFragment extends Fragment {
    private FragmentTicketsBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentTicketsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupTickets();
    }

    private void setupTickets() {
        List<Ticket> tickets = new ArrayList<>();
        tickets.add(new Ticket("Oceanfront Infinity Pool - Day Pass", "Weekday Access, Towel Service", "$150", "Flash Sale: Ends in 2h 35m", ""));
        tickets.add(new Ticket("Sunset Cabana - VIP Weekend", "Private Cabana, Champagne, Butler", "$350", "Flash Sale: Ends in 1h 10m", ""));
        tickets.add(new Ticket("Resort Oasis Pool - Weekday Pass", "Access to main pool and lounge chairs", "$95", "", ""));
        tickets.add(new Ticket("Skyline Rooftop Pool - Evening Pass", "5PM - 10PM, Cocktail included", "$120", "", ""));

        TicketAdapter adapter = new TicketAdapter(tickets);
        binding.rvTickets.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvTickets.setAdapter(adapter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
