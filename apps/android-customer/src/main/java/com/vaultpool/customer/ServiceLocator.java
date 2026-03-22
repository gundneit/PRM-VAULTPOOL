package com.vaultpool.customer;

import com.vaultpool.customer.data.remote.api.PoolApi;
import com.vaultpool.customer.data.repository.PoolRepositoryImpl;
import com.vaultpool.customer.domain.repository.PoolRepository;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Simple Service Locator for Dependency Injection.
 * This replaces Hilt for Java-only projects.
 */
public class ServiceLocator {

    private static ServiceLocator instance;

    private com.vaultpool.customer.domain.repository.AuthRepository authRepository;
    private com.vaultpool.customer.domain.usecase.LoginUseCase loginUseCase;
    private com.vaultpool.customer.data.local.prefs.PreferencesManager preferencesManager;
    private com.vaultpool.customer.data.auth.AuthFlowManager authFlowManager;
    
    // Staff Dependencies
    private com.vaultpool.customer.data.remote.api.StaffApi staffApi;
    private com.vaultpool.customer.domain.repository.StaffRepository staffRepository;
    
    // Pool Dependencies
    private PoolApi poolApi;
    private PoolRepository poolRepository;

    private ServiceLocator() {
    }

    public static synchronized ServiceLocator getInstance() {
        if (instance == null) {
            instance = new ServiceLocator();
        }
        return instance;
    }

    public void init(android.content.Context context) {
        // Initialize common dependencies
        preferencesManager = new com.vaultpool.customer.data.local.prefs.PreferencesManager(context);
        
        // Setup Retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BuildConfig.BACKEND_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                .build();
        
        // Initialize Auth dependencies
        com.vaultpool.customer.data.remote.api.AuthApi authApi = 
                retrofit.create(com.vaultpool.customer.data.remote.api.AuthApi.class);
        authRepository = new com.vaultpool.customer.data.repository.FirebaseAuthRepository(authApi);
        loginUseCase = new com.vaultpool.customer.domain.usecase.LoginUseCase(authRepository);
        authFlowManager = new com.vaultpool.customer.data.auth.AuthFlowManager(
                authRepository,
                preferencesManager
        );
        
        // Initialize Staff dependencies
        staffApi = retrofit.create(com.vaultpool.customer.data.remote.api.StaffApi.class);
        staffRepository = new com.vaultpool.customer.data.repository.StaffRepositoryImpl(staffApi);

        // Initialize Pool dependencies
        poolApi = retrofit.create(PoolApi.class);
        poolRepository = new PoolRepositoryImpl(poolApi);
    }

    public com.vaultpool.customer.domain.repository.AuthRepository getAuthRepository() {
        return authRepository;
    }

    public com.vaultpool.customer.domain.usecase.LoginUseCase getLoginUseCase() {
        return loginUseCase;
    }

    public com.vaultpool.customer.data.local.prefs.PreferencesManager getPreferencesManager() {
        return preferencesManager;
    }

    public com.vaultpool.customer.data.auth.AuthFlowManager getAuthFlowManager() {
        return authFlowManager;
    }
    
    public com.vaultpool.customer.domain.repository.StaffRepository getStaffRepository() {
        return staffRepository;
    }

    public PoolRepository getPoolRepository() {
        return poolRepository;
    }
}
