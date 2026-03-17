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
                viewModel.updatePoolStatus(pool.getId());
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
                Toast.makeText(getContext(), "Slot Operation Successful", Toast.LENGTH_SHORT).show();
            } else if (result.isFailure()) {
                Toast.makeText(getContext(), result.getErrorMessage(), Toast.LENGTH_LONG).show();
            }
        });

        viewModel.getLoading().observe(getViewLifecycleOwner(), loading -> {
            binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        });

        binding.fabAddPool.setOnClickListener(v -> {
            // Can use same fragment for add, just pass null
            openEditFragment(null);
        });
    }

    private void showPoolOptionsDialog(PoolStaffDto pool) {
        DialogPoolOptionsBinding dialogBinding = DialogPoolOptionsBinding.inflate(getLayoutInflater());
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogBinding.getRoot())
                .create();

        // Make background transparent so the card corners show
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
            viewModel.updatePoolStatus(pool.getId());
        });

        dialogBinding.btnAddSlot.setOnClickListener(v -> {
            dialog.dismiss();
            showAddSlotQuickAction(pool);
        });

        dialogBinding.btnClose.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void openEditFragment(@Nullable PoolStaffDto pool) {
        if (pool == null) {
            Toast.makeText(getContext(), "Adding new pool coming soon", Toast.LENGTH_SHORT).show();
            return;
        }
        
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out)
                .add(com.vaultpool.customer.R.id.nav_host_fragment, StaffEditPoolFragment.newInstance(pool))
                .addToBackStack(null)
                .commit();
    }

    private void showAddSlotQuickAction(PoolStaffDto pool) {
        SlotStaffDto newSlot = new SlotStaffDto();
        newSlot.setPoolId(pool.getId());
        newSlot.setStartTime("2026-03-17T08:00:00");
        newSlot.setEndTime("2026-03-17T10:00:00");
        newSlot.setCapacityTotal(20);
        newSlot.setCapacityAvailable(20);
        newSlot.setPrice(150000L);
        newSlot.setStatus("ACTIVE");

        viewModel.createSlot(pool.getId(), newSlot);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
