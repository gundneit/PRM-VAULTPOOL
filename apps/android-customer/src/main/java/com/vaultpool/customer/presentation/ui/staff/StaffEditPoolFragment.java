package com.vaultpool.customer.presentation.ui.staff;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.vaultpool.customer.ServiceLocator;
import com.vaultpool.customer.data.remote.dto.staff.ImageStaffDto;
import com.vaultpool.customer.data.remote.dto.staff.PoolStaffDto;
import com.vaultpool.customer.databinding.FragmentEditPoolBinding;

import java.util.ArrayList;

public class StaffEditPoolFragment extends Fragment {

    private FragmentEditPoolBinding binding;
    private StaffViewModel viewModel;
    private PoolStaffDto pool;
    private StaffPoolImageAdapter imageAdapter;

    public static StaffEditPoolFragment newInstance(PoolStaffDto pool) {
        StaffEditPoolFragment fragment = new StaffEditPoolFragment();
        Bundle args = new Bundle();
        if (pool != null) {
            args.putSerializable("pool", (java.io.Serializable) pool);
        }
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentEditPoolBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        pool = (PoolStaffDto) getArguments().getSerializable("pool");
        if (pool == null) {
            pool = new PoolStaffDto();
            pool.setStatus("ACTIVE");
        }
        
        setupViewModel();
        bindData();
        setupActions();
    }

    private void setupViewModel() {
        StaffViewModelFactory factory = new StaffViewModelFactory(
                ServiceLocator.getInstance().getStaffRepository(),
                ServiceLocator.getInstance().getAuthRepository()
        );
        viewModel = new ViewModelProvider(requireActivity(), factory).get(StaffViewModel.class);
        
        viewModel.getPoolActionUpdate().observe(getViewLifecycleOwner(), result -> {
            if (result.isSuccess()) {
                requireActivity().getSupportFragmentManager().popBackStack();
            }
        });
        
        viewModel.getLoading().observe(getViewLifecycleOwner(), loading -> {
            binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
            binding.btnSave.setEnabled(!loading);
        });
    }

    private void bindData() {
        if (pool.getName() != null) binding.etName.setText(pool.getName());
        if (pool.getAddress() != null) binding.etAddress.setText(pool.getAddress());
        if (pool.getDescription() != null) binding.etDescription.setText(pool.getDescription());
        if (pool.getOpenHours() != null) binding.etHours.setText(pool.getOpenHours());
        
        binding.toolbarEdit.setTitle(pool.getId() == null ? "Add New Pool" : "Edit Pool Info");

        imageAdapter = new StaffPoolImageAdapter();
        binding.rvImages.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvImages.setAdapter(imageAdapter);
        imageAdapter.setImages(pool.getImages());
        imageAdapter.setOnImageRemoveListener(position -> {
            imageAdapter.getImages().remove(position);
            imageAdapter.notifyItemRemoved(position);
        });
    }

    private void setupActions() {
        binding.toolbarEdit.setNavigationOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        binding.btnAddImage.setOnClickListener(v -> showAddImageDialog());

        binding.btnSave.setOnClickListener(v -> {
            pool.setName(binding.etName.getText().toString());
            pool.setAddress(binding.etAddress.getText().toString());
            pool.setDescription(binding.etDescription.getText().toString());
            pool.setOpenHours(binding.etHours.getText().toString());
            pool.setImages(imageAdapter.getImages());
            
            // Default geo coordinates if missing
            if (pool.getGeoLat() == null) pool.setGeoLat(10.762622);
            if (pool.getGeoLng() == null) pool.setGeoLng(106.660172);

            if (pool.getId() == null) {
                viewModel.createPool(pool);
            } else {
                viewModel.updatePool(pool.getId(), pool);
            }
        });
    }

    private void showAddImageDialog() {
        EditText etUrl = new EditText(requireContext());
        etUrl.setHint("https://example.com/pool.jpg");
        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        
        new AlertDialog.Builder(requireContext())
                .setTitle("Add Image URL")
                .setView(etUrl)
                .setPositiveButton("Add", (dialog, which) -> {
                    String url = etUrl.getText().toString().trim();
                    if (!url.isEmpty()) {
                        ImageStaffDto img = new ImageStaffDto();
                        img.setImageUrl(url);
                        img.setSortOrder(imageAdapter.getItemCount() + 1);
                        imageAdapter.addImage(img);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
        
        // Apply padding to the EditText in the dialog
        ViewGroup.LayoutParams lp = etUrl.getLayoutParams();
        if (lp instanceof ViewGroup.MarginLayoutParams) {
            ((ViewGroup.MarginLayoutParams) lp).setMargins(padding, padding, padding, padding);
        }
        etUrl.setPadding(padding, padding, padding, padding);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
