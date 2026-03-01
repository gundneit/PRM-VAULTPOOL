# 🔥 Hướng Dẫn Implement Firebase Authentication cho La Vela Pool Booking

Nếu bạn quyết định dùng Firebase, đây là hướng dẫn chi tiết để implement.

---

## 📋 Mục Lục

1. [Setup Firebase Project](#1-setup-firebase-project)
2. [Android App Configuration](#2-android-app-configuration)
3. [Firebase Authentication Implementation](#3-firebase-authentication-implementation)
4. [Firestore Database Setup](#4-firestore-database-setup)
5. [Code Examples](#5-code-examples)

---

## 1. Setup Firebase Project

### Bước 1: Tạo Firebase Project

1. Vào https://console.firebase.google.com
2. Click **Add project**
3. Nhập tên: `La Vela Pool Booking`
4. Enable Google Analytics (optional)
5. Click **Create project**

### Bước 2: Add Android App

1. Trong Firebase Console → **Project Overview**
2. Click icon Android → **Add app**
3. Nhập:
   - **Package name:** `com.lavelahotel.poolbooking` (phải match với `applicationId` trong `build.gradle.kts`)
   - **App nickname:** `La Vela Pool Booking`
   - **Debug signing certificate:** Optional
4. Click **Register app**
5. Download `google-services.json`
6. Copy file vào `android/app/` folder

### Bước 3: Enable Authentication Methods

1. Firebase Console → **Authentication** → **Get started**
2. **Sign-in method** tab:
   - **Email/Password:** Enable
   - **Phone:** Enable (cho SMS OTP)

### Bước 4: Setup Firestore Database

1. Firebase Console → **Firestore Database** → **Create database**
2. Chọn **Start in test mode** (cho development)
3. Chọn location: `asia-southeast1` (Singapore - gần VN nhất)
4. Click **Enable**

---

## 2. Android App Configuration

### Bước 1: Add Dependencies

**File:** `android/app/build.gradle.kts`

```kotlin
plugins {
    // ... existing plugins
    id("com.google.gms.google-services") // Add this
}

dependencies {
    // ... existing dependencies
    
    // Firebase BOM (Bill of Materials)
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    
    // Firebase Authentication
    implementation("com.google.firebase:firebase-auth-ktx")
    
    // Firestore Database
    implementation("com.google.firebase:firebase-firestore-ktx")
    
    // Firebase Analytics (optional)
    implementation("com.google.firebase:firebase-analytics-ktx")
}
```

**File:** `android/build.gradle.kts` (project-level)

```kotlin
buildscript {
    dependencies {
        classpath("com.google.gms:google-services:4.4.0") // Add this
    }
}
```

### Bước 2: Add google-services.json

1. Copy `google-services.json` vào `android/app/`
2. Sync Gradle

---

## 3. Firebase Authentication Implementation

### 3.1. Email/Password Authentication

**File:** `FirebaseAuthRepository.kt`

```kotlin
package com.lavelahotel.poolbooking.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
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
            val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
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
}
```

### 3.2. Phone Authentication với OTP

**File:** `FirebasePhoneAuthRepository.kt`

```kotlin
package com.lavelahotel.poolbooking.data.repository

import android.app.Activity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit

class FirebasePhoneAuthRepository {
    private val auth = FirebaseAuth.getInstance()
    private var verificationId: String? = null
    
    // Gửi OTP đến số điện thoại (Firebase tự động gửi SMS!)
    suspend fun sendOtpToPhone(
        activity: Activity,
        phoneNumber: String,
        onVerificationCompleted: (PhoneAuthCredential) -> Unit,
        onVerificationFailed: (Exception) -> Unit,
        onCodeSent: (String) -> Unit
    ) {
        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                onVerificationCompleted(credential)
            }
            
            override fun onVerificationFailed(e: Exception) {
                onVerificationFailed(e)
            }
            
            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                this@FirebasePhoneAuthRepository.verificationId = verificationId
                onCodeSent(verificationId)
            }
        }
        
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber) // Format: +84901234567
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)
            .build()
        
        PhoneAuthProvider.verifyPhoneNumber(options)
        // Firebase tự động gửi SMS OTP!
    }
    
    // Verify OTP code
    suspend fun verifyOtpCode(code: String): Result<FirebaseUser> {
        return try {
            val credential = PhoneAuthProvider.getCredential(verificationId!!, code)
            val result = auth.signInWithCredential(credential).await()
            val user = result.user ?: throw Exception("Verification failed")
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Resend OTP
    suspend fun resendOtp(
        activity: Activity,
        phoneNumber: String,
        token: PhoneAuthProvider.ForceResendingToken,
        onCodeSent: (String) -> Unit
    ) {
        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                this@FirebasePhoneAuthRepository.verificationId = verificationId
                onCodeSent(verificationId)
            }
            
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {}
            override fun onVerificationFailed(e: Exception) {}
        }
        
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)
            .setForceResendingToken(token)
            .build()
        
        PhoneAuthProvider.verifyPhoneNumber(options)
    }
}
```

---

## 4. Firestore Database Setup

### 4.1. Data Model

**Collections Structure:**
```
users/
  └─ {userId}/
      ├─ email: "user@example.com"
      ├─ phone: "0901234567"
      ├─ fullName: "Nguyễn Văn A"
      ├─ emailVerified: true
      ├─ phoneVerified: true
      └─ createdAt: Timestamp

bookings/
  └─ {bookingId}/
      ├─ userId: "firebase_user_id"
      ├─ poolSlot: "10:00-11:00"
      ├─ date: Timestamp
      ├─ status: "confirmed"
      └─ createdAt: Timestamp
```

### 4.2. Firestore Repository

**File:** `FirestoreRepository.kt`

```kotlin
package com.lavelahotel.poolbooking.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class FirestoreRepository {
    private val db = FirebaseFirestore.getInstance()
    
    // Lưu user data
    suspend fun saveUserData(
        userId: String,
        email: String,
        phone: String?,
        fullName: String
    ): Result<Unit> {
        return try {
            val userData = mapOf(
                "email" to email,
                "phone" to phone,
                "fullName" to fullName,
                "emailVerified" to false,
                "phoneVerified" to false,
                "createdAt" to com.google.firebase.Timestamp.now()
            )
            
            db.collection("users")
                .document(userId)
                .set(userData, SetOptions.merge())
                .await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Đọc user data
    suspend fun getUserData(userId: String): Result<Map<String, Any>> {
        return try {
            val document = db.collection("users")
                .document(userId)
                .get()
                .await()
            
            if (document.exists()) {
                Result.success(document.data!!)
            } else {
                Result.failure(Exception("User not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Real-time listener
    fun getUserDataRealtime(
        userId: String,
        onUpdate: (Map<String, Any>?) -> Unit
    ) {
        db.collection("users")
            .document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onUpdate(null)
                    return@addSnapshotListener
                }
                
                if (snapshot != null && snapshot.exists()) {
                    onUpdate(snapshot.data)
                } else {
                    onUpdate(null)
                }
            }
    }
}
```

---

## 5. Code Examples

### 5.1. Register với Email (Firebase)

**File:** `RegisterFragment.kt` (Firebase version)

```kotlin
class RegisterFragment : Fragment() {
    private val authRepository = FirebaseAuthRepository()
    private val firestoreRepository = FirestoreRepository()
    
    private fun register() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                showLoading(true)
                
                // 1. Register với Firebase Auth
                val result = authRepository.registerWithEmail(
                    email = binding.etEmail.text.toString(),
                    password = binding.etPassword.text.toString(),
                    fullName = binding.etFullName.text.toString()
                )
                
                result.onSuccess { user ->
                    // 2. Lưu user data vào Firestore
                    firestoreRepository.saveUserData(
                        userId = user.uid,
                        email = user.email!!,
                        phone = binding.etPhone.text.toString().takeIf { it.isNotEmpty() },
                        fullName = binding.etFullName.text.toString()
                    )
                    
                    // 3. Firebase đã tự động gửi email verification!
                    Toast.makeText(context, "Đăng ký thành công. Vui lòng kiểm tra email để verify.", Toast.LENGTH_LONG).show()
                    
                    // 4. Navigate to email verification screen
                    navigateToEmailVerification()
                }.onFailure { error ->
                    showError(error.message ?: "Đăng ký thất bại")
                }
            } finally {
                showLoading(false)
            }
        }
    }
}
```

### 5.2. Phone Authentication với OTP (Firebase)

**File:** `PhoneAuthFragment.kt`

```kotlin
class PhoneAuthFragment : Fragment() {
    private val phoneAuthRepository = FirebasePhoneAuthRepository()
    
    private fun sendOtp() {
        val phoneNumber = "+84${binding.etPhone.text.toString().substring(1)}"
        
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                showLoading(true)
                
                phoneAuthRepository.sendOtpToPhone(
                    activity = requireActivity(),
                    phoneNumber = phoneNumber,
                    onVerificationCompleted = { credential ->
                        // Auto-verify (nếu device có SIM card)
                        verifyCredential(credential)
                    },
                    onVerificationFailed = { error ->
                        showLoading(false)
                        showError(error.message ?: "Gửi OTP thất bại")
                    },
                    onCodeSent = { verificationId ->
                        showLoading(false)
                        Toast.makeText(context, "Mã OTP đã được gửi đến $phoneNumber", Toast.LENGTH_SHORT).show()
                        // Hiển thị OTP input
                        binding.etOtp.visibility = View.VISIBLE
                        binding.btnVerify.visibility = View.VISIBLE
                    }
                )
            } catch (e: Exception) {
                showLoading(false)
                showError(e.message ?: "Lỗi không xác định")
            }
        }
    }
    
    private fun verifyOtp() {
        val code = binding.etOtp.text.toString()
        
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                showLoading(true)
                
                val result = phoneAuthRepository.verifyOtpCode(code)
                result.onSuccess { user ->
                    showLoading(false)
                    Toast.makeText(context, "Xác thực thành công", Toast.LENGTH_SHORT).show()
                    navigateToMain()
                }.onFailure { error ->
                    showLoading(false)
                    showError(error.message ?: "Mã OTP không đúng")
                }
            } catch (e: Exception) {
                showLoading(false)
                showError(e.message ?: "Lỗi không xác định")
            }
        }
    }
}
```

### 5.3. Login (Firebase)

**File:** `LoginFragment.kt` (Firebase version)

```kotlin
class LoginFragment : Fragment() {
    private val authRepository = FirebaseAuthRepository()
    
    private fun login() {
        val email = binding.etEmailOrPhone.text.toString()
        val password = binding.etPassword.text.toString()
        
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                showLoading(true)
                
                val result = authRepository.loginWithEmail(email, password)
                result.onSuccess { user ->
                    if (user.isEmailVerified) {
                        // Đã verify → Navigate to main
                        navigateToMain()
                    } else {
                        // Chưa verify → Navigate to verification screen
                        Toast.makeText(context, "Vui lòng verify email trước", Toast.LENGTH_LONG).show()
                        navigateToEmailVerification()
                    }
                }.onFailure { error ->
                    showError(error.message ?: "Đăng nhập thất bại")
                }
            } finally {
                showLoading(false)
            }
        }
    }
}
```

---

## 📊 So Sánh Code

### Register Flow

**Firebase:**
```kotlin
// ~30 dòng code
authRepository.registerWithEmail(email, password, fullName)
    .onSuccess { user ->
        firestoreRepository.saveUserData(user.uid, ...)
        // Done! Firebase tự động gửi email verification
    }
```

**Current Solution:**
```kotlin
// ~200+ dòng code
// 1. Create RegisterRequest
// 2. Call API với Retrofit
// 3. Backend tạo user
// 4. Backend generate OTP
// 5. Backend gửi email qua Gmail SMTP
// 6. Navigate to OTP screen
// 7. User nhập OTP
// 8. Verify OTP
// 9. Generate tokens
// 10. Save tokens
```

### Phone OTP

**Firebase:**
```kotlin
// Firebase tự động gửi SMS!
PhoneAuthProvider.verifyPhoneNumber(options)
// User nhận SMS tự động
```

**Current Solution:**
```kotlin
// Cần:
// 1. Backend generate OTP
// 2. Backend gọi Twilio API
// 3. Twilio gửi SMS
// 4. User verify OTP
// = 500+ lines code
```

---

## 💰 Cost Comparison

### Firebase Free Tier

**Authentication:**
- ✅ 50K MAU (Monthly Active Users) free
- ✅ Unlimited email verifications free
- ✅ SMS OTP: $0.06/SMS (sau free tier)

**Firestore:**
- ✅ 1GB storage free
- ✅ 50K reads/day free
- ✅ 20K writes/day free

**Sau free tier:**
- 💰 $0.06/SMS
- 💰 $0.18/100K reads
- 💰 $0.18/100K writes

### Current Solution

- 💰 VPS: $5-20/tháng
- 💰 Twilio: $0.01-0.05/SMS
- 💰 Email: Free (Gmail)

**Kết luận:** Firebase rẻ hơn cho <50K users, đắt hơn khi scale lớn.

---

## 🎯 Kết Luận

### Firebase Ưu Điểm:
- ✅ Setup cực kỳ đơn giản
- ✅ Không cần backend server
- ✅ SMS OTP tự động (không cần Twilio)
- ✅ Email verification tự động
- ✅ Offline support built-in
- ✅ Real-time database

### Firebase Nhược Điểm:
- ⚠️ Vendor lock-in
- ⚠️ Cost cao khi scale lớn
- ⚠️ NoSQL (không có SQL queries)
- ⚠️ Khó customize

### Khuyến Nghị:

**Dùng Firebase nếu:**
- ✅ Chỉ cần authentication đơn giản
- ✅ Cần launch nhanh
- ✅ Team nhỏ, không có backend dev
- ✅ Budget hạn chế

**Dùng Current Solution nếu:**
- ✅ Cần complex business logic
- ✅ Cần integration với ERP
- ✅ Cần full control
- ✅ Scale lớn (>100K users)

---

**Bạn muốn tôi tạo full Firebase implementation cho Android app không?** 🚀
