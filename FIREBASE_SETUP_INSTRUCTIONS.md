# 🔥 Hướng Dẫn Setup Firebase cho La Vela Pool Booking

Tài liệu này hướng dẫn chi tiết cách setup Firebase để app có thể hoạt động.

---

## 📋 Bước 1: Tạo Firebase Project

1. **Vào Firebase Console:**
   - Truy cập https://console.firebase.google.com
   - Đăng nhập với Google account

2. **Tạo Project mới:**
   - Click **Add project** hoặc **Create a project**
   - Nhập tên project: `La Vela Pool Booking`
   - Enable Google Analytics (optional - có thể tắt)
   - Click **Create project**

3. **Chờ Firebase tạo project** (vài giây)

---

## 📱 Bước 2: Add Android App vào Firebase

1. **Trong Firebase Console:**
   - Click icon **Android** (hoặc **Add app** → **Android**)

2. **Nhập thông tin:**
   - **Android package name:** `com.lavelahotel.poolbooking`
     - ⚠️ **QUAN TRỌNG:** Phải đúng với `applicationId` trong `build.gradle.kts`
   - **App nickname:** `La Vela Pool Booking` (optional)
   - **Debug signing certificate SHA-1:** (optional, có thể bỏ qua)

3. **Click Register app**

4. **Download `google-services.json`:**
   - Click **Download google-services.json**
   - **Lưu file này vào:** `android/app/google-services.json`
   - ⚠️ **QUAN TRỌNG:** File phải ở đúng vị trí này!

5. **Click Next** → **Next** → **Continue to console**

---

## 🔐 Bước 3: Enable Authentication Methods

1. **Vào Authentication:**
   - Firebase Console → **Authentication** → **Get started**

2. **Enable Email/Password:**
   - Tab **Sign-in method**
   - Click **Email/Password**
   - Enable **Email/Password** (toggle ON)
   - Click **Save**

3. **Email verification (tự động enable):**
   - Firebase tự động gửi email verification
   - Không cần config thêm

---

## 🗄️ Bước 4: Setup Firestore (Optional - cho tương lai)

1. **Vào Firestore Database:**
   - Firebase Console → **Firestore Database** → **Create database**

2. **Chọn mode:**
   - **Start in test mode** (cho development)
   - Click **Next**

3. **Chọn location:**
   - Chọn `asia-southeast1` (Singapore - gần VN nhất)
   - Click **Enable**

4. **Security Rules (sau khi tạo):**
   ```javascript
   rules_version = '2';
   service cloud.firestore {
     match /databases/{database}/documents {
       match /{document=**} {
         allow read, write: if request.auth != null;
       }
     }
   }
   ```

---

## 🔧 Bước 5: Verify Setup trong Android Studio

1. **Kiểm tra `google-services.json`:**
   - File phải ở: `android/app/google-services.json`
   - File phải có package name: `com.lavelahotel.poolbooking`

2. **Sync Gradle:**
   - Android Studio → **File** → **Sync Project with Gradle Files**
   - Hoặc click **Sync Now** nếu có thông báo

3. **Kiểm tra dependencies:**
   - Mở `android/app/build.gradle.kts`
   - Đảm bảo có:
     ```kotlin
     plugins {
         id("com.google.gms.google-services")
     }
     
     dependencies {
         implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
         implementation("com.google.firebase:firebase-auth-ktx")
     }
     ```

4. **Build project:**
   - **Build** → **Make Project**
   - Nếu có lỗi, check lại các bước trên

---

## ✅ Bước 6: Test Authentication

1. **Chạy app:**
   - Kết nối device/emulator
   - Click **Run** (Shift+F10)

2. **Test Register:**
   - Nhập email và password
   - Click **Đăng ký**
   - Kiểm tra email để nhận verification link

3. **Test Login:**
   - Đăng nhập với email/password đã đăng ký
   - Nếu chưa verify email → App sẽ yêu cầu verify

4. **Test Email Verification:**
   - Click link trong email
   - Quay lại app → Click **Đã xác thực**
   - App sẽ navigate đến Main screen

---

## 🐛 Troubleshooting

### Lỗi: "File google-services.json is missing"

**Giải pháp:**
- ✅ Đảm bảo file `google-services.json` ở đúng vị trí: `android/app/google-services.json`
- ✅ File phải có package name đúng: `com.lavelahotel.poolbooking`
- ✅ Sync Gradle lại

### Lỗi: "Default FirebaseApp is not initialized"

**Giải pháp:**
- ✅ Kiểm tra `google-services.json` có đúng không
- ✅ Kiểm tra plugin `com.google.gms.google-services` đã apply chưa
- ✅ Clean và rebuild project

### Lỗi: "The email address is already in use"

**Giải pháp:**
- ✅ Email đã được đăng ký trong Firebase
- ✅ Có thể xóa user trong Firebase Console → Authentication → Users

### Email verification không đến

**Giải pháp:**
- ✅ Check Spam folder
- ✅ Đảm bảo email không có typo
- ✅ Có thể resend email từ app
- ✅ Check Firebase Console → Authentication → Users → Xem user có email verified chưa

### Build error: "Plugin with id 'com.google.gms.google-services' not found"

**Giải pháp:**
- ✅ Kiểm tra `android/build.gradle.kts` có:
    ```kotlin
    plugins {
        id("com.google.gms.google-services") version "4.4.0" apply false
    }
    ```
- ✅ Sync Gradle lại

---

## 📝 Checklist Setup

- [ ] Firebase project đã được tạo
- [ ] Android app đã được add vào Firebase
- [ ] File `google-services.json` đã download và đặt đúng vị trí
- [ ] Email/Password authentication đã enable
- [ ] Gradle đã sync thành công
- [ ] App build thành công
- [ ] Test register thành công
- [ ] Test login thành công
- [ ] Email verification hoạt động

---

## 🎯 Sau Khi Setup Xong

App sẽ có các tính năng:
- ✅ Đăng ký với Email/Password
- ✅ Đăng nhập với Email/Password
- ✅ Email verification tự động
- ✅ Resend verification email
- ✅ Check email verification status
- ✅ Auto-login nếu đã verify

**Không cần:**
- ❌ Backend server
- ❌ Database server
- ❌ Gmail SMTP setup
- ❌ Twilio SMS setup
- ❌ JWT tokens management

**Firebase handle tất cả!** 🎉

---

## 📚 Tài Liệu Tham Khảo

- Firebase Console: https://console.firebase.google.com
- Firebase Auth Docs: https://firebase.google.com/docs/auth
- Firebase Android Setup: https://firebase.google.com/docs/android/setup

---

**Sau khi setup xong, app sẽ hoạt động hoàn toàn với Firebase!** 🚀
