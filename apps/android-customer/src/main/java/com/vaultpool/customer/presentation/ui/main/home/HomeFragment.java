package com.vaultpool.customer.presentation.ui.main.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.vaultpool.customer.databinding.FragmentHomeBinding;
import com.vaultpool.customer.domain.model.Pool;
import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {
    private FragmentHomeBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupPools();
    }

    private void setupPools() {
        List<Pool> pools = new ArrayList<>();
        pools.add(new Pool("Oceanview Infinity", "Malibu, CA", "$150/hr", 4.9f, ""));
        pools.add(new Pool("Hidden Oasis", "Austin, TX", "$85/hr", 4.7f, ""));
        pools.add(new Pool("Sky High Rooftop", "New York, NY", "$200/hr", 4.8f, ""));

        PoolAdapter adapter = new PoolAdapter(pools);
        binding.rvPools.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvPools.setAdapter(adapter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
