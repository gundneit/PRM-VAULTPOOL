package com.vaultpool.customer.presentation.ui.staff;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.vaultpool.customer.R;
import com.vaultpool.customer.ServiceLocator;
import com.vaultpool.customer.data.remote.dto.staff.RevenueAnalyticsDto;
import com.vaultpool.customer.databinding.FragmentStaffAnalyticsBinding;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class StaffAnalyticsFragment extends Fragment {

    private FragmentStaffAnalyticsBinding binding;
    private StaffViewModel viewModel;

    private StaffRevenueTimeAdapter revenueTimeAdapter;
    private StaffRevenuePoolAdapter revenuePoolAdapter;
    private StaffInventoryLogAdapter inventoryLogAdapter;

    private static final DateTimeFormatter API_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.US);
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

    private LocalDate fromDate;
    private LocalDate toDate;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentStaffAnalyticsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupViewModel();
        setupDefaultDates();
        setupGranularitySpinner();
        setupRecyclerViews();
        setupActions();
        observeViewModel();
        fetchAnalytics();
    }

    private void setupViewModel() {
        StaffViewModelFactory factory = new StaffViewModelFactory(
                ServiceLocator.getInstance().getStaffRepository(),
                ServiceLocator.getInstance().getAuthRepository()
        );
        viewModel = new ViewModelProvider(requireActivity(), factory).get(StaffViewModel.class);
    }

    private void setupDefaultDates() {
        LocalDate now = LocalDate.now();
        toDate = now;
        fromDate = now.withDayOfMonth(1);

        renderDateInputs();
    }

    private void setupGranularitySpinner() {
        String[] granularities = new String[]{"DAY", "WEEK", "MONTH"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                granularities
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerGranularity.setAdapter(adapter);
        binding.spinnerGranularity.setSelection(0);
    }

    private void setupRecyclerViews() {
        revenueTimeAdapter = new StaffRevenueTimeAdapter();
        revenuePoolAdapter = new StaffRevenuePoolAdapter();
        inventoryLogAdapter = new StaffInventoryLogAdapter();

        binding.rvRevenueTime.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvRevenueTime.setAdapter(revenueTimeAdapter);

        binding.rvRevenuePool.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvRevenuePool.setAdapter(revenuePoolAdapter);

        binding.rvInventoryLogs.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvInventoryLogs.setAdapter(inventoryLogAdapter);

        binding.toggleAuditType.check(R.id.btn_audit_booking);
        updateAuditInputState();
    }

    private void setupActions() {
        View.OnClickListener fromClick = v -> openDatePicker(true);
        View.OnClickListener toClick = v -> openDatePicker(false);
        binding.etFromDate.setOnClickListener(fromClick);
        binding.etToDate.setOnClickListener(toClick);

        binding.btnApplyFilters.setOnClickListener(v -> {
            if (!isDateRangeValid()) {
                Toast.makeText(getContext(), "Start date must be before or equal to end date", Toast.LENGTH_SHORT).show();
                return;
            }
            fetchAnalytics();
        });

        binding.toggleAuditType.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) {
                return;
            }
            updateAuditInputState();
        });

        binding.btnSearchAudit.setOnClickListener(v -> {
            if (!isDateRangeValid()) {
                Toast.makeText(getContext(), "Start date must be before or equal to end date", Toast.LENGTH_SHORT).show();
                return;
            }
            fetchAuditLogsByCurrentMode();
        });
    }

    private void observeViewModel() {
        viewModel.getRevenueByTime().observe(getViewLifecycleOwner(), result -> {
            if (result.isSuccess() && result.getData() != null) {
                List<RevenueAnalyticsDto> data = result.getData();
                revenueTimeAdapter.setItems(data);
                renderSummary(data);
            } else if (result.isFailure()) {
                Toast.makeText(getContext(), result.getErrorMessage(), Toast.LENGTH_LONG).show();
                revenueTimeAdapter.setItems(null);
                renderSummary(null);
            }
        });

        viewModel.getRevenueByPool().observe(getViewLifecycleOwner(), result -> {
            if (result.isSuccess() && result.getData() != null) {
                revenuePoolAdapter.setItems(result.getData());
            } else if (result.isFailure()) {
                Toast.makeText(getContext(), result.getErrorMessage(), Toast.LENGTH_LONG).show();
                revenuePoolAdapter.setItems(null);
            }
        });

        viewModel.getInventoryLogs().observe(getViewLifecycleOwner(), result -> {
            if (result.isSuccess() && result.getData() != null) {
                inventoryLogAdapter.setItems(result.getData());
                binding.tvAuditCount.setText(result.getData().size() + " records");
            } else if (result.isFailure()) {
                Toast.makeText(getContext(), result.getErrorMessage(), Toast.LENGTH_LONG).show();
                inventoryLogAdapter.setItems(null);
                binding.tvAuditCount.setText("0 records");
            }
        });

        viewModel.getLoading().observe(getViewLifecycleOwner(), loading -> {
            binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        });
    }

    private void fetchAnalytics() {
        String from = fromDate.format(API_DATE_FORMAT);
        String to = toDate.format(API_DATE_FORMAT);
        String granularity = String.valueOf(binding.spinnerGranularity.getSelectedItem());

        viewModel.fetchRevenueByTime(from, to, granularity);
        viewModel.fetchRevenueByPool(from, to);
    }

    private void fetchAuditLogsByCurrentMode() {
        int checkedId = binding.toggleAuditType.getCheckedButtonId();
        if (checkedId == R.id.btn_audit_all) {
            Toast.makeText(getContext(), "System-wide logs (All) require ADMIN permission", Toast.LENGTH_LONG).show();
            return;
        }

        String query = binding.etAuditQuery.getText() == null
                ? ""
                : binding.etAuditQuery.getText().toString().trim();

        if (TextUtils.isEmpty(query)) {
            Toast.makeText(getContext(), "Please enter a lookup value", Toast.LENGTH_SHORT).show();
            return;
        }

        if (checkedId == R.id.btn_audit_slot) {
            try {
                long slotId = Long.parseLong(query);
                viewModel.fetchInventoryLogsBySlot(slotId);
            } catch (NumberFormatException ex) {
                Toast.makeText(getContext(), "Slot ID must be a number", Toast.LENGTH_SHORT).show();
            }
        } else if (checkedId == R.id.btn_audit_booking) {
            viewModel.fetchInventoryLogsByBooking(query.toUpperCase(Locale.US));
        }
    }

    private void updateAuditInputState() {
        int checkedId = binding.toggleAuditType.getCheckedButtonId();
        boolean needsInput = checkedId != R.id.btn_audit_all;

        binding.layoutAuditQuery.setEnabled(needsInput);
        binding.etAuditQuery.setEnabled(needsInput);

        if (!needsInput) {
            binding.layoutAuditQuery.setHint("Booking code or slot ID");
            binding.layoutAuditQuery.setHelperText("All mode requires ADMIN permission");
            return;
        }

        if (checkedId == R.id.btn_audit_slot) {
            binding.layoutAuditQuery.setHint("Enter slot ID");
            binding.layoutAuditQuery.setHelperText("Example: 1001");
        } else {
            binding.layoutAuditQuery.setHint("Enter booking code");
            binding.layoutAuditQuery.setHelperText("Example: VP-6898518F");
        }
    }

    private void renderSummary(List<RevenueAnalyticsDto> data) {
        long totalRevenue = 0L;
        int totalTickets = 0;
        int totalBookings = 0;

        if (data != null) {
            for (RevenueAnalyticsDto item : data) {
                if (item.getTotalRevenue() != null) {
                    totalRevenue += item.getTotalRevenue();
                }
                if (item.getTicketsSold() != null) {
                    totalTickets += item.getTicketsSold();
                }
                if (item.getBookingCount() != null) {
                    totalBookings += item.getBookingCount();
                }
            }
        }

        binding.tvTotalRevenue.setText(currencyFormat.format(totalRevenue));
        binding.tvTotalTickets.setText(String.valueOf(totalTickets));
        binding.tvTotalBookings.setText(String.valueOf(totalBookings));
    }

    private void openDatePicker(boolean isFromDate) {
        LocalDate currentDate = isFromDate ? fromDate : toDate;
        long selection = currentDate
                .atStartOfDay(ZoneOffset.UTC)
                .toInstant()
                .toEpochMilli();

        MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(isFromDate ? "Select start date" : "Select end date")
                .setSelection(selection)
                .build();

        picker.addOnPositiveButtonClickListener(millis -> {
            if (millis == null) {
                return;
            }
            LocalDate pickedDate = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate();
            if (isFromDate) {
                fromDate = pickedDate;
            } else {
                toDate = pickedDate;
            }
            renderDateInputs();
        });

        picker.show(getChildFragmentManager(), isFromDate ? "staff_from_date" : "staff_to_date");
    }

    private boolean isDateRangeValid() {
        if (fromDate == null || toDate == null) {
            return false;
        }
        return !fromDate.isAfter(toDate);
    }

    private void renderDateInputs() {
        binding.etFromDate.setText(fromDate.format(API_DATE_FORMAT));
        binding.etToDate.setText(toDate.format(API_DATE_FORMAT));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
