package com.lavelahotel.poolbooking.ui.main

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.lavelahotel.poolbooking.databinding.ActivityMainBinding
import com.lavelahotel.poolbooking.ui.auth.AuthActivity

class MainActivity : AppCompatActivity() {
    
    companion object {
        private const val TAG = "MainActivity"
    }
    
    private lateinit var binding: ActivityMainBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            binding = ActivityMainBinding.inflate(layoutInflater)
            setContentView(binding.root)
            
            // TODO: Implement main screen UI
            binding.tvWelcome.text = "Chào mừng đến với La Vela Pool Booking!"

            // Nút đăng xuất: signOut Firebase và quay về màn hình đăng nhập
            binding.btnLogout.setOnClickListener {
                try {
                    FirebaseAuth.getInstance().signOut()
                    val intent = Intent(this, AuthActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    startActivity(intent)
                    finish()
                } catch (e: Exception) {
                    Log.e(TAG, "Error while logging out", e)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in onCreate", e)
            throw e // Re-throw để Android có thể log lỗi
        }
    }
}
