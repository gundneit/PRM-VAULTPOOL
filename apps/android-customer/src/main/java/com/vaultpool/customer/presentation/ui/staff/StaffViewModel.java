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
    private final MutableLiveData<Result<List<SlotStaffDto>>> slots = new MutableLiveData<>();
    private final MutableLiveData<Result<PoolStaffDto>> poolActionUpdate = new MutableLiveData<>();
    private final MutableLiveData<Result<SlotStaffDto>> slotActionUpdate = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);

    public StaffViewModel(StaffRepository staffRepository, AuthRepository authRepository) {
        this.staffRepository = staffRepository;
        this.authRepository = authRepository;
    }

    public LiveData<Result<List<BookingStaffDto>>> getBookings() { return bookings; }
    public LiveData<Result<List<PoolStaffDto>>> getPools() { return pools; }
    public LiveData<Result<List<SlotStaffDto>>> getSlots() { return slots; }
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

    public void fetchSlots(Long poolId) {
        loading.setValue(true);
        disposables.add(authRepository.getIdToken()
                .flatMap(token -> staffRepository.getPoolSlots(token, poolId))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    loading.setValue(false);
                    slots.setValue(result);
                }, throwable -> {
                    loading.setValue(false);
                    slots.setValue(Result.failure(throwable.getMessage()));
                }));
    }

    public void fetchSlotsByDate(Long poolId, String date) {
        loading.setValue(true);
        disposables.add(staffRepository.getSlotsByDate(poolId, date)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    loading.setValue(false);
                    slots.setValue(result);
                }, throwable -> {
                    loading.setValue(false);
                    slots.setValue(Result.failure(throwable.getMessage()));
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

    public void updatePoolStatus(Long poolId, String currentStatus) {
        String newStatus = "ACTIVE".equalsIgnoreCase(currentStatus) ? "INACTIVE" : "ACTIVE";
        loading.setValue(true);
        disposables.add(authRepository.getIdToken()
                .flatMap(token -> staffRepository.updatePoolStatus(token, poolId, newStatus))
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
                    if (result.isFailure()) {
                        String msg = result.getErrorMessage();
                        if (msg != null && (msg.contains("400") || msg.toLowerCase().contains("bad request"))) {
                            msg = "This slot has expired and cannot be edited.";
                        }
                        slotActionUpdate.setValue(Result.failure(msg));
                    } else {
                        slotActionUpdate.setValue(result);
                    }
                }, throwable -> {
                    loading.setValue(false);
                    String message = throwable.getMessage();
                    if (throwable instanceof retrofit2.HttpException) {
                        if (((retrofit2.HttpException) throwable).code() == 400) {
                            message = "This slot has expired and cannot be edited.";
                        }
                    } else if (message != null && (message.contains("400") || message.toLowerCase().contains("bad request"))) {
                        message = "This slot has expired and cannot be edited.";
                    }
                    slotActionUpdate.setValue(Result.failure(message));
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
                    if (result.isFailure()) {
                        String msg = result.getErrorMessage();
                        if (msg != null && (msg.contains("400") || msg.toLowerCase().contains("bad request"))) {
                            msg = "This slot has expired and cannot be edited.";
                        }
                        slotActionUpdate.setValue(Result.failure(msg));
                    } else {
                        slotActionUpdate.setValue(result);
                    }
                }, throwable -> {
                    loading.setValue(false);
                    String message = throwable.getMessage();
                    if (throwable instanceof retrofit2.HttpException) {
                        if (((retrofit2.HttpException) throwable).code() == 400) {
                            message = "This slot has expired and cannot be edited.";
                        }
                    } else if (message != null && (message.contains("400") || message.toLowerCase().contains("bad request"))) {
                        message = "This slot has expired and cannot be edited.";
                    }
                    slotActionUpdate.setValue(Result.failure(message));
                }));
    }

    public void updateSlotStatus(Long poolId, Long slotId, String currentStatus) {
        String newStatus;
        if ("ACTIVE".equalsIgnoreCase(currentStatus)) {
            newStatus = "INACTIVE";
        } else {
            newStatus = "ACTIVE";
        }

        loading.setValue(true);
        disposables.add(authRepository.getIdToken()
                .flatMap(token -> staffRepository.updateSlotStatus(token, poolId, slotId, newStatus))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    loading.setValue(false);
                    if (result.isFailure()) {
                        String msg = result.getErrorMessage();
                        if (msg != null && (msg.contains("400") || msg.toLowerCase().contains("bad request"))) {
                            msg = "This slot has expired and cannot be edited.";
                        }
                        slotActionUpdate.setValue(Result.failure(msg));
                    } else {
                        slotActionUpdate.setValue(result);
                    }
                }, throwable -> {
                    loading.setValue(false);
                    String message = throwable.getMessage();
                    if (throwable instanceof retrofit2.HttpException) {
                        if (((retrofit2.HttpException) throwable).code() == 400) {
                            message = "This slot has expired and cannot be edited.";
                        }
                    } else if (message != null && (message.contains("400") || message.toLowerCase().contains("bad request"))) {
                        message = "This slot has expired and cannot be edited.";
                    }
                    slotActionUpdate.setValue(Result.failure(message));
                }));
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        disposables.clear();
    }
}
