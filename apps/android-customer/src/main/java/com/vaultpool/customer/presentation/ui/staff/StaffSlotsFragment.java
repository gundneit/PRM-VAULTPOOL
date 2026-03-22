package com.vaultpool.customer.presentation.ui.staff;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;

public class StaffSlotsFragment extends Fragment {

    private static final DateTimeFormatter API_DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter API_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private FragmentStaffSlotsBinding binding;
    private StaffViewModel viewModel;
    private StaffSlotAdapter adapter;
    private PoolStaffDto pool;
    private String selectedDate;
    private boolean pendingCreateSlotAction;

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

        selectedDate = LocalDate.now().format(API_DATE_FORMATTER);
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
        try {
            LocalDate parsedDate = LocalDate.parse(selectedDate, API_DATE_FORMATTER);
            cal.set(parsedDate.getYear(), parsedDate.getMonthValue() - 1, parsedDate.getDayOfMonth());
        } catch (Exception ignored) {}

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    applySelectedDate(year, month, dayOfMonth);
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.getDatePicker().init(
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH),
                (view, year, monthOfYear, dayOfMonth) -> applySelectedDate(year, monthOfYear, dayOfMonth)
        );

        datePickerDialog.setOnShowListener(dialog -> {
            if (datePickerDialog.getButton(DialogInterface.BUTTON_NEGATIVE) != null) {
                datePickerDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setText("Back");
            }
            if (datePickerDialog.getButton(DialogInterface.BUTTON_POSITIVE) != null) {
                datePickerDialog.getButton(DialogInterface.BUTTON_POSITIVE).setText("Select");
            }
        });

        datePickerDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Back", (dialog, which) -> dialog.dismiss());
        datePickerDialog.show();
    }

    private void applySelectedDate(int year, int monthZeroBased, int dayOfMonth) {
        String newDate = LocalDate.of(year, monthZeroBased + 1, dayOfMonth).format(API_DATE_FORMATTER);
        if (!newDate.equals(selectedDate)) {
            selectedDate = newDate;
            binding.tvSelectedDate.setText(selectedDate);
            refreshSlots();
        }
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

        LocalDate defaultDate = LocalDate.parse(selectedDate, API_DATE_FORMATTER);
        final LocalDate[] slotDate = new LocalDate[]{defaultDate};
        final LocalTime[] startTime = new LocalTime[]{LocalTime.of(8, 0)};
        final LocalTime[] endTime = new LocalTime[]{LocalTime.of(10, 0)};

        if (slotToEdit != null) {
            // Edit mode
            dialogBinding.tvDialogTitle.setText("Edit Slot");
            dialogBinding.btnCreate.setText("Update");

            LocalDateTime parsedStart = tryParseApiDateTime(slotToEdit.getStartTime());
            LocalDateTime parsedEnd = tryParseApiDateTime(slotToEdit.getEndTime());
            if (parsedStart != null) {
                slotDate[0] = parsedStart.toLocalDate();
                startTime[0] = parsedStart.toLocalTime();
            }
            if (parsedEnd != null) {
                endTime[0] = parsedEnd.toLocalTime();
            }

            dialogBinding.etSlotDate.setText(slotDate[0].format(API_DATE_FORMATTER));
            dialogBinding.etStartTime.setText(startTime[0].format(TIME_FORMATTER));
            dialogBinding.etEndTime.setText(endTime[0].format(TIME_FORMATTER));
            dialogBinding.etPrice.setText(String.valueOf(slotToEdit.getPrice()));
            dialogBinding.etCapacity.setText(String.valueOf(slotToEdit.getCapacityTotal()));
        } else {
            // Add mode - default to selected date
            dialogBinding.tvDialogTitle.setText("Create New Slot");
            dialogBinding.etSlotDate.setText(slotDate[0].format(API_DATE_FORMATTER));
            dialogBinding.etStartTime.setText(startTime[0].format(TIME_FORMATTER));
            dialogBinding.etEndTime.setText(endTime[0].format(TIME_FORMATTER));
            dialogBinding.etPrice.setText("150000");
            dialogBinding.etCapacity.setText("20");
        }

        dialogBinding.etSlotDate.setOnClickListener(v -> showSlotDatePicker(slotDate, dialogBinding));
        dialogBinding.etStartTime.setOnClickListener(v -> showSlotTimePicker(startTime, dialogBinding.etStartTime));
        dialogBinding.etEndTime.setOnClickListener(v -> showSlotTimePicker(endTime, dialogBinding.etEndTime));

        dialogBinding.btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialogBinding.btnCreate.setOnClickListener(v -> {
            SlotStaffDto slot = slotToEdit != null ? slotToEdit : new SlotStaffDto();
            slot.setPoolId(pool.getId());

            try {
                LocalDate selectedSlotDate = LocalDate.parse(dialogBinding.etSlotDate.getText().toString().trim(), API_DATE_FORMATTER);
                LocalTime selectedStartTime = LocalTime.parse(dialogBinding.etStartTime.getText().toString().trim(), TIME_FORMATTER);
                LocalTime selectedEndTime = LocalTime.parse(dialogBinding.etEndTime.getText().toString().trim(), TIME_FORMATTER);

                LocalDateTime startDateTime = LocalDateTime.of(selectedSlotDate, selectedStartTime);
                LocalDateTime endDateTime = LocalDateTime.of(selectedSlotDate, selectedEndTime);

                if (!endDateTime.isAfter(startDateTime)) {
                    Toast.makeText(getContext(), "End time must be after start time", Toast.LENGTH_SHORT).show();
                    return;
                }

                slot.setStartTime(startDateTime.format(API_DATE_TIME_FORMATTER));
                slot.setEndTime(endDateTime.format(API_DATE_TIME_FORMATTER));

                slot.setPrice(Long.parseLong(dialogBinding.etPrice.getText().toString().trim()));
                slot.setCapacityTotal(Integer.parseInt(dialogBinding.etCapacity.getText().toString().trim()));
                if (slotToEdit == null) {
                    slot.setCapacityAvailable(slot.getCapacityTotal());
                    slot.setStatus("ACTIVE");
                    pendingCreateSlotAction = true;
                    viewModel.createSlot(pool.getId(), slot);
                } else {
                    pendingCreateSlotAction = false;
                    viewModel.updateSlot(pool.getId(), slot.getId(), slot);
                }
                dialog.dismiss();
            } catch (Exception e) {
                Toast.makeText(getContext(), "Invalid input format", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private void showSlotDatePicker(LocalDate[] slotDate, DialogAddSlotBinding dialogBinding) {
        DatePickerDialog pickerDialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    slotDate[0] = LocalDate.of(year, month + 1, dayOfMonth);
                    dialogBinding.etSlotDate.setText(slotDate[0].format(API_DATE_FORMATTER));
                },
                slotDate[0].getYear(),
                slotDate[0].getMonthValue() - 1,
                slotDate[0].getDayOfMonth()
        );
        pickerDialog.show();
    }

    private void showSlotTimePicker(LocalTime[] timeValue, com.google.android.material.textfield.TextInputEditText targetInput) {
        TimePickerDialog pickerDialog = new TimePickerDialog(
                requireContext(),
                (view, hourOfDay, minute) -> {
                    timeValue[0] = LocalTime.of(hourOfDay, minute);
                    targetInput.setText(timeValue[0].format(TIME_FORMATTER));
                },
                timeValue[0].getHour(),
                timeValue[0].getMinute(),
                true
        );
        pickerDialog.show();
    }

    @Nullable
    private LocalDateTime tryParseApiDateTime(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        String trimmed = value.trim();
        try {
            return LocalDateTime.parse(trimmed);
        } catch (Exception ignored) {
        }

        try {
            return OffsetDateTime.parse(trimmed).toLocalDateTime();
        } catch (Exception ignored) {
        }

        try {
            if (trimmed.length() >= 19) {
                return LocalDateTime.parse(trimmed.substring(0, 19), API_DATE_TIME_FORMATTER);
            }
        } catch (Exception ignored) {
        }
        return null;
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
                String successMessage = pendingCreateSlotAction ? "Create slot successful" : "Update successful";
                Toast.makeText(getContext(), successMessage, Toast.LENGTH_SHORT).show();
                pendingCreateSlotAction = false;
            } else if (result.isFailure()) {
                // The ViewModel now provides the formatted error message
                Toast.makeText(getContext(), result.getErrorMessage(), Toast.LENGTH_LONG).show();
                pendingCreateSlotAction = false;
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