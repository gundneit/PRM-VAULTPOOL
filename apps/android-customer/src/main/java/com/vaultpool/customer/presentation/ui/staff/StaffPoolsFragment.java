package com.vaultpool.customer.presentation.ui.staff;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
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
import com.vaultpool.customer.databinding.DialogAddSlotBinding;
import com.vaultpool.customer.databinding.DialogPoolOptionsBinding;
import com.vaultpool.customer.databinding.FragmentStaffPoolsBinding;

public class StaffPoolsFragment extends Fragment {

    private FragmentStaffPoolsBinding binding;
    private StaffViewModel viewModel;
    private StaffPoolAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentStaffPoolsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupViewModel();
        setupRecyclerView();
        observeViewModel();
        viewModel.fetchPools();
    }

    private void setupViewModel() {
        StaffViewModelFactory factory = new StaffViewModelFactory(
                ServiceLocator.getInstance().getStaffRepository(),
                ServiceLocator.getInstance().getAuthRepository()
        );
        viewModel = new ViewModelProvider(requireActivity(), factory).get(StaffViewModel.class);
    }

    private void setupRecyclerView() {
        adapter = new StaffPoolAdapter();
        adapter.setOnPoolClickListener(new StaffPoolAdapter.OnPoolClickListener() {
            @Override
            public void onPoolClick(PoolStaffDto pool) {
                showPoolOptionsDialog(pool);
            }

            @Override
            public void onStatusToggle(PoolStaffDto pool) {
                viewModel.updatePoolStatus(pool.getId(), pool.getStatus());
            }
        });
        
        binding.rvPools.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvPools.setAdapter(adapter);
    }

    private void observeViewModel() {
        viewModel.getPools().observe(getViewLifecycleOwner(), result -> {
            if (result.isSuccess() && result.getData() != null) {
                adapter.setPools(result.getData());
                binding.tvPoolCount.setText(result.getData().size() + " facilities active");
            } else if (result.isFailure()) {
                Toast.makeText(getContext(), result.getErrorMessage(), Toast.LENGTH_LONG).show();
            }
        });

        viewModel.getPoolActionUpdate().observe(getViewLifecycleOwner(), result -> {
            if (result.isSuccess()) {
                Toast.makeText(getContext(), "Pool Updated Successfully", Toast.LENGTH_SHORT).show();
            } else if (result.isFailure()) {
                Toast.makeText(getContext(), result.getErrorMessage(), Toast.LENGTH_LONG).show();
            }
        });

        viewModel.getSlotActionUpdate().observe(getViewLifecycleOwner(), result -> {
            if (result.isSuccess()) {
                Toast.makeText(getContext(), "Slot Created Successfully", Toast.LENGTH_SHORT).show();
            } else if (result.isFailure()) {
                Toast.makeText(getContext(), result.getErrorMessage(), Toast.LENGTH_LONG).show();
            }
        });

        viewModel.getLoading().observe(getViewLifecycleOwner(), loading -> {
            binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        });

        binding.fabAddPool.setOnClickListener(v -> {
            openEditFragment(null);
        });
    }

    private void showPoolOptionsDialog(PoolStaffDto pool) {
        DialogPoolOptionsBinding dialogBinding = DialogPoolOptionsBinding.inflate(getLayoutInflater());
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogBinding.getRoot())
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        dialogBinding.tvPoolTitle.setText(pool.getName());
        
        dialogBinding.btnEditInfo.setOnClickListener(v -> {
            dialog.dismiss();
            openEditFragment(pool);
        });

        dialogBinding.btnToggleStatus.setOnClickListener(v -> {
            dialog.dismiss();
            viewModel.updatePoolStatus(pool.getId(), pool.getStatus());
        });

        dialogBinding.btnAddSlot.setOnClickListener(v -> {
            dialog.dismiss();
            showAddSlotDialog(pool);
        });

        dialogBinding.btnViewSlots.setOnClickListener(v -> {
            dialog.dismiss();
            openSlotsFragment(pool);
        });

        dialogBinding.btnClose.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void openEditFragment(@Nullable PoolStaffDto pool) {
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out)
                .add(com.vaultpool.customer.R.id.nav_host_fragment, StaffEditPoolFragment.newInstance(pool))
                .addToBackStack(null)
                .commit();
    }

    private void openSlotsFragment(PoolStaffDto pool) {
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(com.vaultpool.customer.R.anim.slide_in_right, com.vaultpool.customer.R.anim.slide_out_left, com.vaultpool.customer.R.anim.slide_in_right, com.vaultpool.customer.R.anim.slide_out_left)
                .add(com.vaultpool.customer.R.id.nav_host_fragment, StaffSlotsFragment.newInstance(pool))
                .addToBackStack(null)
                .commit();
    }

    private void showAddSlotDialog(PoolStaffDto pool) {
        DialogAddSlotBinding slotBinding = DialogAddSlotBinding.inflate(getLayoutInflater());
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(slotBinding.getRoot())
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        // Set default values for easier testing
        slotBinding.etStartTime.setText("2026-03-18T08:00:00");
        slotBinding.etEndTime.setText("2026-03-18T10:00:00");
        slotBinding.etPrice.setText("150000");
        slotBinding.etCapacity.setText("20");

        slotBinding.btnCancel.setOnClickListener(v -> dialog.dismiss());

        slotBinding.btnCreate.setOnClickListener(v -> {
            SlotStaffDto newSlot = new SlotStaffDto();
            newSlot.setPoolId(pool.getId());
            newSlot.setStartTime(slotBinding.etStartTime.getText().toString());
            newSlot.setEndTime(slotBinding.etEndTime.getText().toString());
            
            try {
                newSlot.setPrice(Long.parseLong(slotBinding.etPrice.getText().toString()));
                newSlot.setCapacityTotal(Integer.parseInt(slotBinding.etCapacity.getText().toString()));
                newSlot.setCapacityAvailable(newSlot.getCapacityTotal());
                newSlot.setStatus("ACTIVE");
                
                viewModel.createSlot(pool.getId(), newSlot);
                dialog.dismiss();
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Invalid input format", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
