# 📱 Hướng Dẫn Cấu Hình Firebase Phone Authentication

## 🔧 Bước 1: Bật Phone Authentication trong Firebase Console

### 1.1. Truy cập Firebase Console
1. Mở trình duyệt và vào: https://console.firebase.google.com/
2. Chọn project của bạn (hoặc tạo project mới nếu chưa có)

### 1.2. Bật Phone Authentication
1. Trong menu bên trái, click **Authentication**
2. Click tab **Sign-in method**
3. Tìm **Phone** trong danh sách providers
4. Click vào **Phone**
5. Bật **Enable** toggle
6. Click **Save**

---

## 🔑 Bước 2: Thêm SHA-1 và SHA-256 Fingerprints

**QUAN TRỌNG:** Firebase Phone Auth yêu cầu SHA-1 và SHA-256 fingerprints để hoạt động.

### 2.1. Lấy SHA-1 và SHA-256 từ Android Studio

**Cách 1: Từ Android Studio (Dễ nhất)**
1. Mở Android Studio
2. Click vào **Gradle** tab ở bên phải
3. Mở rộng: `android` → `app` → `Tasks` → `android`
4. Double-click vào **signingReport**
5. Xem output ở dưới, tìm dòng:
   ```
   SHA1: XX:XX:XX:...
   SHA256: XX:XX:XX:...
   ```
6. Copy cả 2 giá trị

**Cách 2: Từ Command Line (Windows)**
```powershell
# Mở PowerShell trong thư mục android
cd android

# Lấy SHA-1 (Debug keystore)
keytool -list -v -keystore "%USERPROFILE%\.android\debug.keystore" -alias androiddebugkey -storepass android -keypass android

# Hoặc lấy SHA-1 từ gradle
.\gradlew signingReport
```

### 2.2. Thêm Fingerprints vào Firebase
1. Vào Firebase Console → **Project Settings** (icon ⚙️)
2. Scroll xuống phần **Your apps**
3. Tìm app Android của bạn (package: `com.lavelahotel.poolbooking`)
4. Click vào app
5. Trong phần **SHA certificate fingerprints**, click **Add fingerprint**
6. Paste **SHA-1** → **Save**
7. Click **Add fingerprint** lần nữa
8. Paste **SHA-256** → **Save**

---

## 📱 Bước 3: Cấu Hình Test Phone Numbers (Tùy chọn)

Để test Phone Auth mà không tốn tiền SMS:

1. Vào Firebase Console → **Authentication** → **Sign-in method**
2. Click vào **Phone**
3. Scroll xuống phần **Phone numbers for testing**
4. Click **Add phone number**
5. Nhập:
   - **Phone number**: `+841234567890` (format: +84 + số điện thoại)
   - **Verification code**: `123456` (mã OTP test)
6. Click **Add**
7. Lặp lại để thêm nhiều số test nếu cần

**Lưu ý:** 
- Số test chỉ hoạt động trong **development mode**
- Format số: `+84` (mã quốc gia) + số điện thoại (bỏ số 0 đầu tiên)
- Ví dụ: `0912345678` → `+84912345678`

---

## 🔐 Bước 4: Kiểm Tra Permissions trong AndroidManifest

Đảm bảo file `AndroidManifest.xml` có các permissions sau:

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.READ_PHONE_STATE" />
```

**Lưu ý:** 
- `READ_PHONE_STATE` cần cho Phone Auth (để tự động đọc SMS OTP)
- Permission này sẽ được request runtime trên Android 6.0+

---

## 🧪 Bước 5: Test Phone Authentication

### 5.1. Test với Test Phone Number
1. Run app trên emulator hoặc device
2. Đăng ký → Chọn **SMS**
3. Nhập số test (ví dụ: `+841234567890`)
4. Nhập mã OTP test (`123456`)
5. Xác thực thành công → Vào MainActivity

### 5.2. Test với Số Thật
1. Nhập số điện thoại thật (format: `+84912345678`)
2. Nhận SMS OTP từ Firebase
3. Nhập mã OTP
4. Xác thực thành công

---

## ⚠️ Lưu Ý Quan Trọng

### 1. Format Số Điện Thoại
- **Bắt buộc có mã quốc gia**: `+84` (Việt Nam)
- **Bỏ số 0 đầu tiên**: `0912345678` → `+84912345678`
- **Không có khoảng trắng**: `+84 912 345 678` ❌ → `+84912345678` ✅

### 2. reCAPTCHA
- Firebase Phone Auth tự động hiển thị reCAPTCHA để chống spam
- Trên emulator: có thể tự động pass
- Trên device thật: user cần verify reCAPTCHA

### 3. Quota và Giới Hạn
- **Free tier**: 10,000 SMS/tháng
- Sau đó: $0.06/SMS
- Test numbers: không tính vào quota

### 4. SHA Fingerprints
- **Debug keystore**: Dùng khi develop
- **Release keystore**: Cần thêm SHA khi build release APK
- Nếu không thêm SHA → Phone Auth sẽ **KHÔNG HOẠT ĐỘNG**

---

## 🔍 Troubleshooting

### Lỗi: "This app is not authorized to use Firebase Authentication"
**Giải pháp:** 
- Kiểm tra SHA-1/SHA-256 đã được thêm vào Firebase chưa
- Đảm bảo package name đúng (`com.lavelahotel.poolbooking`)

### Lỗi: "Invalid phone number format"
**Giải pháp:**
- Đảm bảo số có format: `+84xxxxxxxxx`
- Không có khoảng trắng hoặc ký tự đặc biệt

### Lỗi: "SMS quota exceeded"
**Giải pháp:**
- Dùng test phone numbers để test
- Hoặc upgrade Firebase plan

### Lỗi: "reCAPTCHA verification failed"
**Giải pháp:**
- Kiểm tra internet connection
- Thử lại sau vài phút
- Đảm bảo SHA fingerprints đã được thêm

---

## ✅ Checklist

Trước khi test Phone Auth, đảm bảo:

- [ ] Phone Authentication đã được **Enable** trong Firebase Console
- [ ] **SHA-1** đã được thêm vào Firebase Project Settings
- [ ] **SHA-256** đã được thêm vào Firebase Project Settings
- [ ] Package name đúng: `com.lavelahotel.poolbooking`
- [ ] `google-services.json` đã được thêm vào `app/` folder
- [ ] Permissions đã được thêm vào `AndroidManifest.xml`
- [ ] Đã sync Gradle sau khi thêm SHA

---

## 📚 Tài Liệu Tham Khảo

- Firebase Phone Auth Docs: https://firebase.google.com/docs/auth/android/phone-auth
- Firebase Console: https://console.firebase.google.com/
- SHA Fingerprint Guide: https://developers.google.com/android/guides/client-auth

---

**Sau khi hoàn thành các bước trên, Phone Authentication sẽ hoạt động!** ✅
