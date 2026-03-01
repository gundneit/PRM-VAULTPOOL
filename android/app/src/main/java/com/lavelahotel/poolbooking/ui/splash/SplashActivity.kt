package com.lavelahotel.poolbooking.ui.splash

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.lavelahotel.poolbooking.R
import com.lavelahotel.poolbooking.ui.auth.AuthActivity
import com.lavelahotel.poolbooking.ui.main.MainActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashActivity : AppCompatActivity() {
    
    companion object {
        private const val TAG = "SplashActivity"
        private const val SPLASH_DELAY = 2000L // 2 seconds
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate started")
        
        try {
            super.onCreate(savedInstanceState)
            Log.d(TAG, "super.onCreate() completed")
        } catch (e: Exception) {
            Log.e(TAG, "Error in super.onCreate()", e)
            throw e
        }
        
        try {
            Log.d(TAG, "Setting content view...")
            setContentView(R.layout.activity_splash)
            Log.d(TAG, "Content view set successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error setting content view", e)
            // Nếu không set được content view, app sẽ crash
            // Nhưng ít nhất ta đã log được lỗi
            throw e
        }
        
        try {
            // Sử dụng Coroutines thay vì Handler (modern approach)
            Log.d(TAG, "Starting coroutine...")
            lifecycleScope.launch {
                Log.d(TAG, "Coroutine started, waiting ${SPLASH_DELAY}ms")
                delay(SPLASH_DELAY)
                navigateToNextScreen()
            }
            Log.d(TAG, "onCreate completed successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error starting coroutine", e)
            // Fallback: navigate immediately
            navigateToAuth()
        }
    }
    
    private fun navigateToNextScreen() {
        try {
            // Check if user is logged in với Firebase
            val auth = FirebaseAuth.getInstance()
            val currentUser = auth.currentUser
            
            val intent = if (currentUser != null && currentUser.isEmailVerified) {
                // User đã đăng nhập và đã verify email
                Log.d(TAG, "User logged in, navigating to MainActivity")
                Intent(this, MainActivity::class.java)
            } else {
                // Chưa đăng nhập hoặc chưa verify
                Log.d(TAG, "User not logged in, navigating to AuthActivity")
                Intent(this, AuthActivity::class.java)
            }
            
            startActivity(intent)
            finish()
        } catch (e: Exception) {
            Log.e(TAG, "Error navigating to next screen", e)
            // Fallback: navigate to AuthActivity
            navigateToAuth()
        }
    }
    
    private fun navigateToAuth() {
        try {
            val intent = Intent(this, AuthActivity::class.java)
            startActivity(intent)
            finish()
        } catch (e: Exception) {
            Log.e(TAG, "Critical error: Cannot navigate to AuthActivity", e)
            finish()
        }
    }
}
