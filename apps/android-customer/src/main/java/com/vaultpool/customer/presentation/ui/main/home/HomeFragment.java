package com.vaultpool.customer.presentation.ui.main.home;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.vaultpool.customer.ServiceLocator;
import com.vaultpool.customer.data.remote.dto.PoolDto;
import com.vaultpool.customer.databinding.FragmentHomeBinding;
import com.vaultpool.customer.domain.repository.PoolRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private PoolRepository poolRepository;
    private PoolAdapter poolAdapter;
    private List<PoolDto> originalPools = new ArrayList<>();
    private final CompositeDisposable disposables = new CompositeDisposable();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        poolRepository = ServiceLocator.getInstance().getPoolRepository();
        
        setupRecyclerView();
        setupSearch();
        fetchActivePools();
    }

    private void setupRecyclerView() {
        binding.rvPools.setLayoutManager(new LinearLayoutManager(getContext()));
        poolAdapter = new PoolAdapter(new ArrayList<>());
        binding.rvPools.setAdapter(poolAdapter);
    }

    private void setupSearch() {
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterPools(s.toString());
                binding.ivClearSearch.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        binding.ivClearSearch.setOnClickListener(v -> {
            binding.etSearch.setText("");
        });
    }

    private void filterPools(String query) {
        if (query.isEmpty()) {
            poolAdapter.updateData(originalPools);
            return;
        }

        String lowerCaseQuery = query.toLowerCase().trim();
        List<PoolDto> filteredList = originalPools.stream()
                .filter(pool -> (pool.getName() != null && pool.getName().toLowerCase().contains(lowerCaseQuery)) ||
                                (pool.getAddress() != null && pool.getAddress().toLowerCase().contains(lowerCaseQuery)))
                .collect(Collectors.toList());

        poolAdapter.updateData(filteredList);
    }

    private void fetchActivePools() {
        disposables.add(
                poolRepository.getActivePools()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(response -> {
                            if (response.isSuccess() && response.getData() != null) {
                                originalPools = response.getData();
                                poolAdapter.updateData(originalPools);
                            } else {
                                Toast.makeText(getContext(), response.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }, throwable -> {
                            Toast.makeText(getContext(), "Error: " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                        })
        );
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        disposables.clear();
        binding = null;
    }
}
