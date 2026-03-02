package com.lavelahotel.poolbooking;

import android.app.Application;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;

public class LaVelaApplication extends Application {

    private static final String TAG = "LaVelaApplication";

    @Override
    public void onCreate() {
        super.onCreate();

        try {
            Log.d(TAG, "Application onCreate started");

            try {
                if (FirebaseApp.getApps(this).isEmpty()) {
                    FirebaseApp.initializeApp(this);
                    Log.d(TAG, "Firebase initialized successfully");
                } else {
                    Log.d(TAG, "Firebase already initialized");
                }
            } catch (Exception e) {
                Log.e(TAG, "Firebase initialization failed", e);
            }

            try {
                FirebaseAuth auth = FirebaseAuth.getInstance();
                Log.d(TAG, "FirebaseAuth instance created successfully");
            } catch (Exception e) {
                Log.e(TAG, "FirebaseAuth creation failed", e);
            }

            Log.d(TAG, "Application onCreate completed");
        } catch (Exception e) {
            Log.e(TAG, "Critical error in Application onCreate", e);
        }
    }
}

