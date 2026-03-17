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

import com.vaultpool.customer.ServiceLocator;
import com.vaultpool.customer.data.remote.dto.staff.PoolStaffDto;
import com.vaultpool.customer.databinding.FragmentEditPoolBinding;

public class StaffEditPoolFragment extends Fragment {

    private FragmentEditPoolBinding binding;
    private StaffViewModel viewModel;
    private PoolStaffDto pool;

    public static StaffEditPoolFragment newInstance(PoolStaffDto pool) {
        StaffEditPoolFragment fragment = new StaffEditPoolFragment();
        Bundle args = new Bundle();
        args.putSerializable("pool", (java.io.Serializable) pool);
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
        binding.etName.setText(pool.getName());
        binding.etAddress.setText(pool.getAddress());
        binding.etDescription.setText(pool.getDescription());
        binding.etHours.setText(pool.getOpenHours());
    }

    private void setupActions() {
        binding.toolbarEdit.setNavigationOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        binding.btnSave.setOnClickListener(v -> {
            pool.setName(binding.etName.getText().toString());
            pool.setAddress(binding.etAddress.getText().toString());
            pool.setDescription(binding.etDescription.getText().toString());
            pool.setOpenHours(binding.etHours.getText().toString());
            
            viewModel.updatePool(pool.getId(), pool);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
