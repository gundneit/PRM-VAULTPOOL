package com.vaultpool.customer;

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

    private ServiceLocator() {
    }

    public static synchronized ServiceLocator getInstance() {
        if (instance == null) {
            instance = new ServiceLocator();
        }
        return instance;
    }

    public void init(android.content.Context context) {
        // Initialize dependencies
        preferencesManager = new com.vaultpool.customer.data.local.prefs.PreferencesManager(context);
        authRepository = new com.vaultpool.customer.data.repository.FirebaseAuthRepository();
        loginUseCase = new com.vaultpool.customer.domain.usecase.LoginUseCase(authRepository);
        authFlowManager = new com.vaultpool.customer.data.auth.AuthFlowManager(
                authRepository,
                preferencesManager
        );
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
}
