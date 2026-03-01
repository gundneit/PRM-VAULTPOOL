package com.lavelahotel.poolbooking.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.tasks.await

class FirebaseAuthRepository {
    private val auth = FirebaseAuth.getInstance()
    
    // Register với Email/Password
    suspend fun registerWithEmail(
        email: String,
        password: String,
        fullName: String
    ): Result<FirebaseUser> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user ?: throw Exception("User creation failed")
            
            // Update display name
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(fullName)
                .build()
            user.updateProfile(profileUpdates).await()
            
            // Gửi email verification tự động
            user.sendEmailVerification().await()
            
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Login với Email/Password
    suspend fun loginWithEmail(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val user = result.user ?: throw Exception("Login failed")
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Check email verified
    fun isEmailVerified(): Boolean {
        return auth.currentUser?.isEmailVerified ?: false
    }
    
    // Reload user để refresh emailVerified status
    suspend fun reloadUser(): Result<FirebaseUser> {
        return try {
            val user = auth.currentUser ?: throw Exception("No user logged in")
            user.reload().await()
            Result.success(auth.currentUser!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Resend verification email
    suspend fun resendVerificationEmail(): Result<Unit> {
        return try {
            val user = auth.currentUser ?: throw Exception("No user logged in")
            user.sendEmailVerification().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Get current user
    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }
    
    // Logout
    fun logout() {
        auth.signOut()
    }
    
    // Send password reset email
    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
