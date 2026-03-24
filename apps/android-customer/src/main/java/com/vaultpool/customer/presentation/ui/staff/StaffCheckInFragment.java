package com.vaultpool.customer.presentation.ui.staff;

import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;
import com.vaultpool.customer.ServiceLocator;
import com.vaultpool.customer.data.remote.dto.staff.BookingStaffDto;
import com.vaultpool.customer.databinding.FragmentStaffCheckinBinding;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StaffCheckInFragment extends Fragment {

    private static final Pattern BOOKING_CODE_PATTERN = Pattern.compile("VP-[A-Z0-9]+", Pattern.CASE_INSENSITIVE);

    private FragmentStaffCheckinBinding binding;
    private StaffViewModel viewModel;

    private final ActivityResultLauncher<ScanOptions> scanLauncher = registerForActivityResult(
            new ScanContract(),
            result -> {
                if (result == null || result.getContents() == null) {
                    Toast.makeText(getContext(), "Scan cancelled", Toast.LENGTH_SHORT).show();
                    return;
                }
                handleScannedPayload(result.getContents());
            }
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentStaffCheckinBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupViewModel();
        setupActions();
        observeViewModel();
    }

    private void setupViewModel() {
        StaffViewModelFactory factory = new StaffViewModelFactory(
                ServiceLocator.getInstance().getStaffRepository(),
                ServiceLocator.getInstance().getAuthRepository()
        );
        viewModel = new ViewModelProvider(requireActivity(), factory).get(StaffViewModel.class);
    }

    private void setupActions() {
        binding.btnScanQr.setOnClickListener(v -> launchQrScanner());
        binding.btnCheckIn.setOnClickListener(v -> {
            String rawCode = binding.etBookingCode.getText() == null
                    ? ""
                    : binding.etBookingCode.getText().toString().trim();
            if (TextUtils.isEmpty(rawCode)) {
                Toast.makeText(getContext(), "Please enter or scan booking code", Toast.LENGTH_SHORT).show();
                return;
            }
            String bookingCode = extractBookingCode(rawCode);
            binding.etBookingCode.setText(bookingCode);
            viewModel.checkInBooking(bookingCode);
        });
    }

    private void observeViewModel() {
        viewModel.getCheckInUpdate().observe(getViewLifecycleOwner(), result -> {
            if (result.isSuccess() && result.getData() != null) {
                renderBookingResult(result.getData());
                Toast.makeText(getContext(), "Check-in success", Toast.LENGTH_SHORT).show();
            } else if (result.isFailure()) {
                Toast.makeText(getContext(), result.getErrorMessage(), Toast.LENGTH_LONG).show();
            }
        });

        viewModel.getLoading().observe(getViewLifecycleOwner(), loading -> {
            binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        });
    }

    private void launchQrScanner() {
        ScanOptions options = new ScanOptions();
        options.setDesiredBarcodeFormats(ScanOptions.QR_CODE);
        options.setPrompt("Scan booking QR to check-in");
        options.setBeepEnabled(true);
        options.setOrientationLocked(false);
        scanLauncher.launch(options);
    }

    private void handleScannedPayload(String rawPayload) {
        String bookingCode = extractBookingCode(rawPayload);
        if (TextUtils.isEmpty(bookingCode)) {
            Toast.makeText(getContext(), "Invalid QR payload", Toast.LENGTH_LONG).show();
            return;
        }

        binding.etBookingCode.setText(bookingCode);
        viewModel.checkInBooking(bookingCode);
    }

    private String extractBookingCode(String payload) {
        if (payload == null) {
            return "";
        }

        String trimmed = payload.trim();
        if (trimmed.isEmpty()) {
            return "";
        }

        if (trimmed.startsWith("{")) {
            try {
                JsonObject object = JsonParser.parseString(trimmed).getAsJsonObject();
                if (object.has("bookingCode") && !object.get("bookingCode").isJsonNull()) {
                    return object.get("bookingCode").getAsString().trim();
                }
            } catch (Exception ignored) {
                // Ignore invalid JSON and fallback to other extraction strategies.
            }
        }

        try {
            Uri uri = Uri.parse(trimmed);
            String queryBookingCode = uri.getQueryParameter("bookingCode");
            if (!TextUtils.isEmpty(queryBookingCode)) {
                return queryBookingCode.trim();
            }
        } catch (Exception ignored) {
            // Ignore invalid URI and continue parsing plain text.
        }

        Matcher matcher = BOOKING_CODE_PATTERN.matcher(trimmed);
        if (matcher.find()) {
            return matcher.group().toUpperCase();
        }

        return trimmed;
    }

    private void renderBookingResult(BookingStaffDto booking) {
        binding.tvEmptyResult.setVisibility(View.GONE);
        binding.cardResult.setVisibility(View.VISIBLE);

        binding.tvResultBookingCode.setText(valueOrDash(booking.getBookingCode()));
        binding.tvResultPool.setText(valueOrDash(booking.getPoolName()));
        binding.tvResultAddress.setText(valueOrDash(booking.getPoolAddress()));
        binding.tvResultTime.setText(valueOrDash(booking.getStartTime()) + " - " + valueOrDash(booking.getEndTime()));
        binding.tvResultQty.setText(booking.getQty() == null ? "-" : String.valueOf(booking.getQty()));
        binding.tvResultAmount.setText(booking.getAmount() == null ? "-" : String.valueOf(booking.getAmount()));
        binding.tvResultStatus.setText(valueOrDash(booking.getStatus()));
    }

    private String valueOrDash(String value) {
        return TextUtils.isEmpty(value) ? "-" : value;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
