package com.vaultpool.customer.presentation.ui.staff;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
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
import com.vaultpool.customer.databinding.FragmentStaffSlotsBinding;
import com.vaultpool.customer.domain.model.Result;

import java.util.Calendar;
import java.util.Locale;

public class StaffSlotsFragment extends Fragment {

    private FragmentStaffSlotsBinding binding;
    private StaffViewModel viewModel;
    private StaffSlotAdapter adapter;
    private PoolStaffDto pool;
    private String selectedDate;

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
        
        // Default date
        selectedDate = "2026-03-18";
        binding.tvSelectedDate.setText(selectedDate);
        
        setupViewModel();
        setupRecyclerView();
        observeViewModel();
        setupActions();
        
        binding.toolbarSlots.setTitle("Slots: " + pool.getName());
        binding.toolbarSlots.setNavigationOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());
        
        refreshSlots();
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
        adapter.setOnSlotClickListener(new StaffSlotAdapter.OnSlotClickListener() {
            @Override
            public void onStatusToggle(SlotStaffDto slot) {
                viewModel.updateSlotStatus(pool.getId(), slot.getId(), slot.getStatus());
            }

            @Override
            public void onSlotClick(SlotStaffDto slot) {
                showSlotDialog(slot);
            }
        });
        
        binding.rvSlots.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvSlots.setAdapter(adapter);
    }

    private void setupActions() {
        binding.fabAddSlot.setOnClickListener(v -> showSlotDialog(null));
        binding.btnChangeDate.setOnClickListener(v -> showDatePicker());
    }

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        // If we want to start from the currently selected date:
        try {
            String[] parts = selectedDate.split("-");
            cal.set(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]) - 1, Integer.parseInt(parts[2]));
        } catch (Exception ignored) {}

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                com.vaultpool.customer.R.style.Theme_VaultPool_DatePicker,
                (view, year, month, dayOfMonth) -> {
                    selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);
                    binding.tvSelectedDate.setText(selectedDate);
                    refreshSlots();
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void refreshSlots() {
        if (selectedDate != null) {
            viewModel.fetchSlotsByDate(pool.getId(), selectedDate);
        } else {
            viewModel.fetchSlots(pool.getId());
        }
    }

    private void showSlotDialog(@Nullable SlotStaffDto slotToEdit) {
        DialogAddSlotBinding dialogBinding = DialogAddSlotBinding.inflate(getLayoutInflater());
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogBinding.getRoot())
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        if (slotToEdit != null) {
            // Edit mode
            dialogBinding.tvDialogTitle.setText("Edit Slot");
            dialogBinding.btnCreate.setText("Update");
            dialogBinding.etStartTime.setText(slotToEdit.getStartTime());
            dialogBinding.etEndTime.setText(slotToEdit.getEndTime());
            dialogBinding.etPrice.setText(String.valueOf(slotToEdit.getPrice()));
            dialogBinding.etCapacity.setText(String.valueOf(slotToEdit.getCapacityTotal()));
        } else {
            // Add mode - default to selected date
            dialogBinding.tvDialogTitle.setText("Create New Slot");
            dialogBinding.etStartTime.setText(selectedDate + "T08:00:00");
            dialogBinding.etEndTime.setText(selectedDate + "T10:00:00");
            dialogBinding.etPrice.setText("150000");
            dialogBinding.etCapacity.setText("20");
        }

        dialogBinding.btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialogBinding.btnCreate.setOnClickListener(v -> {
            SlotStaffDto slot = slotToEdit != null ? slotToEdit : new SlotStaffDto();
            slot.setPoolId(pool.getId());
            slot.setStartTime(dialogBinding.etStartTime.getText().toString());
            slot.setEndTime(dialogBinding.etEndTime.getText().toString());
            
            try {
                slot.setPrice(Long.parseLong(dialogBinding.etPrice.getText().toString()));
                slot.setCapacityTotal(Integer.parseInt(dialogBinding.etCapacity.getText().toString()));
                if (slotToEdit == null) {
                    slot.setCapacityAvailable(slot.getCapacityTotal());
                    slot.setStatus("ACTIVE");
                    viewModel.createSlot(pool.getId(), slot);
                } else {
                    viewModel.updateSlot(pool.getId(), slot.getId(), slot);
                }
                dialog.dismiss();
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Invalid input format", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
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
                refreshSlots();
                Toast.makeText(getContext(), "Update successful", Toast.LENGTH_SHORT).show();
            } else if (result.isFailure()) {
                // The ViewModel now provides the formatted error message
                Toast.makeText(getContext(), result.getErrorMessage(), Toast.LENGTH_LONG).show();
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