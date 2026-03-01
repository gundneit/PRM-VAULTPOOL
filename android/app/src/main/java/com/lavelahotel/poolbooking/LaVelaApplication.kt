package com.lavelahotel.poolbooking

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth

class LaVelaApplication : Application() {
    
    companion object {
        private const val TAG = "LaVelaApplication"
    }
    
    override fun onCreate() {
        super.onCreate()
        
        try {
            Log.d(TAG, "Application onCreate started")
            
            // Explicit Firebase initialization với error handling
            try {
                if (FirebaseApp.getApps(this).isEmpty()) {
                    FirebaseApp.initializeApp(this)
                    Log.d(TAG, "Firebase initialized successfully")
                } else {
                    Log.d(TAG, "Firebase already initialized")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Firebase initialization failed", e)
                // Không throw exception để app vẫn có thể chạy
            }
            
            // Test Firebase Auth
            try {
                val auth = FirebaseAuth.getInstance()
                Log.d(TAG, "FirebaseAuth instance created successfully")
            } catch (e: Exception) {
                Log.e(TAG, "FirebaseAuth creation failed", e)
            }
            
            Log.d(TAG, "Application onCreate completed")
        } catch (e: Exception) {
            Log.e(TAG, "Critical error in Application onCreate", e)
            // Không throw để có thể xem log
        }
    }
}
