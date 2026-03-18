package com.vaultpool.customer.presentation.ui.staff;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.vaultpool.customer.ServiceLocator;
import com.vaultpool.customer.data.remote.dto.staff.PoolStaffDto;
import com.vaultpool.customer.data.remote.dto.staff.SlotStaffDto;
import com.vaultpool.customer.databinding.FragmentStaffSlotsBinding;
import com.vaultpool.customer.domain.model.Result;

public class StaffSlotsFragment extends Fragment {

    private FragmentStaffSlotsBinding binding;
    private StaffViewModel viewModel;
    private StaffSlotAdapter adapter;
    private PoolStaffDto pool;

    public static StaffSlotsFragment newInstance(PoolStaffDto pool) {
        StaffSlotsFragment fragment = new StaffSlotsFragment();
        Bundle args = new Bundle();
        args.putSerializable("pool", pool);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentStaffSlotsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        pool = (PoolStaffDto) getArguments().getSerializable("pool");
        
        setupViewModel();
        setupRecyclerView();
        observeViewModel();
        
        binding.toolbarSlots.setTitle("Slots: " + pool.getName());
        binding.toolbarSlots.setNavigationOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());
        
        viewModel.fetchSlots(pool.getId());
    }

    private void setupViewModel() {
        StaffViewModelFactory factory = new StaffViewModelFactory(
                ServiceLocator.getInstance().getStaffRepository(),
                ServiceLocator.getInstance().getAuthRepository()
        );
        viewModel = new ViewModelProvider(requireActivity(), factory).get(StaffViewModel.class);
    }

    private void setupRecyclerView() {
        adapter = new StaffSlotAdapter();
        adapter.setOnSlotClickListener(slot -> {
            viewModel.updateSlotStatus(pool.getId(), slot.getId(), slot.getStatus());
        });
        
        binding.rvSlots.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvSlots.setAdapter(adapter);
    }

    private void observeViewModel() {
        viewModel.getSlots().observe(getViewLifecycleOwner(), result -> {
            if (result.isSuccess() && result.getData() != null) {
                adapter.setSlots(result.getData());
            } else if (result.isFailure()) {
                Toast.makeText(getContext(), result.getErrorMessage(), Toast.LENGTH_LONG).show();
            }
        });

        viewModel.getSlotActionUpdate().observe(getViewLifecycleOwner(), result -> {
            if (result.isSuccess()) {
                viewModel.fetchSlots(pool.getId());
            }
        });

        viewModel.getLoading().observe(getViewLifecycleOwner(), loading -> {
            binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}