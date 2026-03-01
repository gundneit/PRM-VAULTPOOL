package com.lavelahotel.poolbooking.ui.auth

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import com.lavelahotel.poolbooking.R
import com.lavelahotel.poolbooking.databinding.ActivityAuthBinding

class AuthActivity : AppCompatActivity() {
    
    companion object {
        private const val TAG = "AuthActivity"
    }
    
    private lateinit var binding: ActivityAuthBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            binding = ActivityAuthBinding.inflate(layoutInflater)
            setContentView(binding.root)
            
            // Show login fragment by default
            if (savedInstanceState == null) {
                supportFragmentManager.commit {
                    replace(R.id.fragmentContainer, LoginFragment())
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in onCreate", e)
            throw e // Re-throw để Android có thể log lỗi
        }
    }
    
    fun navigateToRegister() {
        supportFragmentManager.commit {
            replace(R.id.fragmentContainer, RegisterFragment())
            addToBackStack(null)
        }
    }
    
    fun navigateToLogin() {
        supportFragmentManager.commit {
            replace(R.id.fragmentContainer, LoginFragment())
            addToBackStack(null)
        }
    }
    
    fun navigateToEmailVerification(email: String) {
        val fragment = EmailVerificationFragment.newInstance(email)
        supportFragmentManager.commit {
            replace(R.id.fragmentContainer, fragment)
            addToBackStack(null)
        }
    }

    fun navigateToPhoneOtp(phone: String) {
        val fragment = PhoneOtpVerificationFragment.newInstance(phone)
        supportFragmentManager.commit {
            replace(R.id.fragmentContainer, fragment)
            addToBackStack(null)
        }
    }
}
