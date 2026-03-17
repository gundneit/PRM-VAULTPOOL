package com.vaultpool.customer.presentation.ui.staff;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.vaultpool.customer.data.remote.dto.staff.BookingStaffDto;
import com.vaultpool.customer.data.remote.dto.staff.PoolStaffDto;
import com.vaultpool.customer.data.remote.dto.staff.SlotStaffDto;
import com.vaultpool.customer.domain.model.Result;
import com.vaultpool.customer.domain.repository.AuthRepository;
import com.vaultpool.customer.domain.repository.StaffRepository;

import java.util.List;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class StaffViewModel extends ViewModel {

    private final StaffRepository staffRepository;
    private final AuthRepository authRepository;
    private final CompositeDisposable disposables = new CompositeDisposable();

    private final MutableLiveData<Result<List<BookingStaffDto>>> bookings = new MutableLiveData<>();
    private final MutableLiveData<Result<List<PoolStaffDto>>> pools = new MutableLiveData<>();
    private final MutableLiveData<Result<PoolStaffDto>> poolActionUpdate = new MutableLiveData<>();
    private final MutableLiveData<Result<SlotStaffDto>> slotActionUpdate = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);

    public StaffViewModel(StaffRepository staffRepository, AuthRepository authRepository) {
        this.staffRepository = staffRepository;
        this.authRepository = authRepository;
    }

    public LiveData<Result<List<BookingStaffDto>>> getBookings() { return bookings; }
    public LiveData<Result<List<PoolStaffDto>>> getPools() { return pools; }
    public LiveData<Result<PoolStaffDto>> getPoolActionUpdate() { return poolActionUpdate; }
    public LiveData<Result<SlotStaffDto>> getSlotActionUpdate() { return slotActionUpdate; }
    public LiveData<Boolean> getLoading() { return loading; }

    public void fetchBookings() {
        loading.setValue(true);
        disposables.add(authRepository.getIdToken()
                .flatMap(token -> staffRepository.getStaffBookings(token))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    loading.setValue(false);
                    bookings.setValue(result);
                }, throwable -> {
                    loading.setValue(false);
                    bookings.setValue(Result.failure(throwable.getMessage()));
                }));
    }

    public void fetchPools() {
        loading.setValue(true);
        disposables.add(authRepository.getIdToken()
                .flatMap(token -> staffRepository.getStaffPools(token))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    loading.setValue(false);
                    pools.setValue(result);
                }, throwable -> {
                    loading.setValue(false);
                    pools.setValue(Result.failure(throwable.getMessage()));
                }));
    }

    public void createPool(PoolStaffDto pool) {
        loading.setValue(true);
        disposables.add(authRepository.getIdToken()
                .flatMap(token -> staffRepository.createPool(token, pool))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    loading.setValue(false);
                    poolActionUpdate.setValue(result);
                    if (result.isSuccess()) fetchPools();
                }, throwable -> {
                    loading.setValue(false);
                    poolActionUpdate.setValue(Result.failure(throwable.getMessage()));
                }));
    }

    public void updatePool(Long id, PoolStaffDto pool) {
        loading.setValue(true);
        disposables.add(authRepository.getIdToken()
                .flatMap(token -> staffRepository.updatePool(token, id, pool))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    loading.setValue(false);
                    poolActionUpdate.setValue(result);
                    if (result.isSuccess()) fetchPools();
                }, throwable -> {
                    loading.setValue(false);
                    poolActionUpdate.setValue(Result.failure(throwable.getMessage()));
                }));
    }

    public void updatePoolStatus(Long poolId) {
        loading.setValue(true);
        disposables.add(authRepository.getIdToken()
                .flatMap(token -> staffRepository.updatePoolStatus(token, poolId))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    loading.setValue(false);
                    poolActionUpdate.setValue(result);
                    if (result.isSuccess()) fetchPools();
                }, throwable -> {
                    loading.setValue(false);
                    poolActionUpdate.setValue(Result.failure(throwable.getMessage()));
                }));
    }

    public void createSlot(Long poolId, SlotStaffDto slot) {
        loading.setValue(true);
        disposables.add(authRepository.getIdToken()
                .flatMap(token -> staffRepository.createSlot(token, poolId, slot))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    loading.setValue(false);
                    slotActionUpdate.setValue(result);
                }, throwable -> {
                    loading.setValue(false);
                    slotActionUpdate.setValue(Result.failure(throwable.getMessage()));
                }));
    }

    public void updateSlot(Long poolId, Long slotId, SlotStaffDto slot) {
        loading.setValue(true);
        disposables.add(authRepository.getIdToken()
                .flatMap(token -> staffRepository.updateSlot(token, poolId, slotId, slot))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    loading.setValue(false);
                    slotActionUpdate.setValue(result);
                }, throwable -> {
                    loading.setValue(false);
                    slotActionUpdate.setValue(Result.failure(throwable.getMessage()));
                }));
    }

    public void updateSlotStatus(Long poolId, Long slotId) {
        loading.setValue(true);
        disposables.add(authRepository.getIdToken()
                .flatMap(token -> staffRepository.updateSlotStatus(token, poolId, slotId))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    loading.setValue(false);
                    slotActionUpdate.setValue(result);
                }, throwable -> {
                    loading.setValue(false);
                    slotActionUpdate.setValue(Result.failure(throwable.getMessage()));
                }));
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        disposables.clear();
    }
}
