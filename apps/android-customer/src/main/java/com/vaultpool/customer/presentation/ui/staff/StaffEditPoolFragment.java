package com.vaultpool.customer.presentation.ui.staff;

import android.content.ContentResolver;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.webkit.MimeTypeMap;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.vaultpool.customer.ServiceLocator;
import com.vaultpool.customer.data.remote.dto.staff.ImageStaffDto;
import com.vaultpool.customer.data.remote.dto.staff.PoolStaffDto;
import com.vaultpool.customer.databinding.FragmentEditPoolBinding;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;

public class StaffEditPoolFragment extends Fragment {

    private FragmentEditPoolBinding binding;
    private StaffViewModel viewModel;
    private PoolStaffDto pool;
    private StaffPoolImageAdapter imageAdapter;
    private boolean pendingSaveAction = false;
    private ActivityResultLauncher<String> pickImageLauncher;
    private File selectedImageFile;

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
        if (getArguments() != null) {
            pool = (PoolStaffDto) getArguments().getSerializable("pool");
        }
        if (pool == null) {
            pool = new PoolStaffDto();
            pool.setStatus("ACTIVE");
        }

        setupImagePicker();
        
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
            if (!pendingSaveAction || result == null) {
                return;
            }

            if (result.isSuccess()) {
                pendingSaveAction = false;
                requireActivity().getSupportFragmentManager().popBackStack();
            } else if (result.isFailure()) {
                pendingSaveAction = false;
                Toast.makeText(getContext(), result.getErrorMessage(), Toast.LENGTH_LONG).show();
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
            selectedImageFile = null;
        });
    }

    private void setupActions() {
        binding.toolbarEdit.setNavigationOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        binding.btnAddImage.setOnClickListener(v -> pickImageLauncher.launch("image/*"));

        binding.btnSave.setOnClickListener(v -> {
            pool.setName(binding.etName.getText().toString());
            pool.setAddress(binding.etAddress.getText().toString());
            pool.setDescription(binding.etDescription.getText().toString());
            pool.setOpenHours(binding.etHours.getText().toString());
            pool.setImages(imageAdapter.getImages());
            
            // Default geo coordinates if missing
            if (pool.getGeoLat() == null) pool.setGeoLat(10.762622);
            if (pool.getGeoLng() == null) pool.setGeoLng(106.660172);

            pendingSaveAction = true;

            if (pool.getId() == null) {
                viewModel.createPool(pool, selectedImageFile);
            } else {
                viewModel.updatePool(pool.getId(), pool, selectedImageFile);
            }
        });
    }

    private void setupImagePicker() {
        pickImageLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri == null) {
                return;
            }

            File cachedFile = copyUriToCache(uri);
            if (cachedFile == null) {
                Toast.makeText(getContext(), "Cannot read selected image", Toast.LENGTH_LONG).show();
                return;
            }

            selectedImageFile = cachedFile;

            ImageStaffDto image = new ImageStaffDto();
            image.setImageUrl(uri.toString());
            image.setSortOrder(1);
            imageAdapter.setImages(Collections.singletonList(image));
        });
    }

    private File copyUriToCache(Uri uri) {
        ContentResolver resolver = requireContext().getContentResolver();
        String extension = resolver.getType(uri) != null
                ? MimeTypeMap.getSingleton().getExtensionFromMimeType(resolver.getType(uri))
                : null;
        if (extension == null || extension.trim().isEmpty()) {
            extension = "jpg";
        }

        String name = queryDisplayName(uri);
        if (name == null || name.trim().isEmpty()) {
            name = "pool_upload_" + System.currentTimeMillis() + "." + extension;
        }

        File target = new File(requireContext().getCacheDir(), name);
        try (InputStream in = resolver.openInputStream(uri);
             FileOutputStream out = new FileOutputStream(target, false)) {
            if (in == null) {
                return null;
            }
            byte[] buffer = new byte[8192];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            out.flush();
            return target;
        } catch (IOException e) {
            return null;
        }
    }

    private String queryDisplayName(Uri uri) {
        String[] projection = new String[]{OpenableColumns.DISPLAY_NAME};
        try (android.database.Cursor cursor = requireContext()
                .getContentResolver()
                .query(uri, projection, null, null, null)) {
            if (cursor == null || !cursor.moveToFirst()) {
                return null;
            }
            int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            if (nameIndex < 0) {
                return null;
            }
            return cursor.getString(nameIndex);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
