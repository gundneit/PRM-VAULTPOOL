# 🏊 La Vela Pool Booking - Android App

Ứng dụng Android đặt vé hồ bơi khách sạn La Vela Saigon Hotel với Firebase Authentication.

## 📋 Tính Năng

- ✅ **Đăng ký tài khoản** với Email/Password
- ✅ **Đăng nhập** với Email/Password
- ✅ **Chọn phương thức xác thực**: Email hoặc **SMS (Firebase Phone Auth)**
- ✅ **Email verification** tự động (Firebase)
- ✅ **OTP SMS** với Firebase Phone Authentication
- ✅ **Resend verification email / resend OTP**
- ✅ **Auto-login** nếu đã verify
- ✅ **Material Design 3** UI/UX
- ✅ **MVVM Architecture** với StateFlow
- ✅ **Firebase Authentication** - Không cần backend server

## 🛠 Công Nghệ

- **Kotlin** - Ngôn ngữ lập trình
- **Firebase Authentication** - Email/Password auth
- **Material Design 3** - UI components
- **MVVM Architecture** - ViewModel + StateFlow
- **Coroutines** - Asynchronous operations
- **View Binding** - Type-safe view references

## 📁 Cấu Trúc Dự Án

```
android/app/src/main/
├── java/com/lavelahotel/poolbooking/
│   ├── data/
│   │   └── repository/
│   │       └── FirebaseAuthRepository.kt  # Firebase Auth
│   ├── ui/
│   │   ├── auth/              # Authentication screens & flow
│   │   │   ├── AuthActivity.kt
│   │   │   ├── LoginFragment.kt
│   │   │   ├── RegisterFragment.kt
│   │   │   ├── EmailVerificationFragment.kt
│   │   │   ├── PhoneOtpVerificationFragment.kt   # Firebase Phone Auth (SMS OTP)
│   │   │   ├── FirebaseAuthViewModel.kt
│   │   │   └── FirebaseAuthViewModelFactory.kt
│   │   ├── splash/            # Splash screen
│   │   └── main/              # Main screen
│   ├── util/                  # Utility classes
│   └── LaVelaApplication.kt  # Application class
└── res/
    ├── layout/                # XML layouts
    ├── values/                # Strings, colors, themes
    └── drawable/              # Drawable resources
```

## 🚀 Cài Đặt

### Yêu Cầu

- Android Studio Hedgehog | 2023.1.1 hoặc mới hơn
- JDK 17
- Android SDK 24+ (minSdk)
- Android SDK 34 (targetSdk)
- Firebase project (xem `FIREBASE_SETUP_INSTRUCTIONS.md`)

### Cài Đặt

1. **Setup Firebase (Bắt buộc):**
   - Xem `FIREBASE_SETUP_INSTRUCTIONS.md` và `FIREBASE_PHONE_AUTH_SETUP.md` ở root project
   - Tạo Firebase project tại https://console.firebase.google.com
   - Add Android app với package name: `com.lavelahotel.poolbooking`
   - Download `google-services.json` → Đặt vào `android/app/`
   - Enable **Email/Password** và **Phone** authentication trong Firebase Console
   - Thêm SHA-1 & SHA-256 fingerprints (signingReport hoặc script `get_sha_fingerprints.ps1`)

2. **Mở project trong Android Studio:**
   - File → Open → Chọn thư mục `android`

3. **Sync Gradle:**
   - Android Studio sẽ tự động sync
   - Hoặc click "Sync Now" nếu có thông báo
   - Đảm bảo không có lỗi về `google-services.json`

4. **Chạy ứng dụng:**
   - Kết nối thiết bị Android hoặc khởi động emulator
   - Click Run (Shift+F10)

## ⚙️ Cấu Hình

### Firebase Configuration

File `google-services.json` đã được tự động load bởi Google Services plugin.

**Không cần config thêm gì!** Firebase tự động handle:
- Authentication
- Email verification
- User management

### Build Variants

- **Debug**: Development build
- **Release**: Production build (minify disabled mặc định)

## 📱 Màn Hình

### 1. Splash Screen
- Hiển thị logo và tên app
- Kiểm tra Firebase Auth state để quyết định điều hướng
- Nếu đã login và verify → Main screen
- Nếu chưa → Auth screen

### 2. Login Screen
- Input: Email, Mật khẩu
- Link: Đăng ký, Quên mật khẩu
- Validation: Email format, password length
- Firebase tự động verify credentials

### 3. Register Screen
- Input: Họ tên, Email, Số điện thoại, Mật khẩu, Xác nhận mật khẩu
- Lựa chọn phương thức xác thực: **Email** hoặc **SMS**
- Validation: Tất cả fields, password match, email & phone format, bắt buộc chọn kênh xác thực
- Với Email: Firebase tạo user + gửi email verification
- Với SMS: Chuyển sang Phone OTP screen (Firebase Phone Auth)

### 4. Email Verification Screen
- Hiển thị email đã đăng ký
- Hướng dẫn user check email và click link
- Button: "Gửi lại email xác thực", "Đã xác thực"
- Check verification status khi click "Đã xác thực"

## 🔐 Authentication Flow (Tóm tắt)

1. **Splash Screen** → Kiểm tra Firebase Auth
   - Đã login & email verified → Main Screen
   - Ngược lại → Auth Screen (Login/Register)

2. **Register Flow:**
   - Nhập thông tin, chọn Email hoặc SMS
   - Nếu Email:
     - Firebase tạo user + gửi email verification
     - Quay về Login để user đăng nhập sau khi verify
   - Nếu SMS:
     - Chuyển sang Phone OTP screen
     - Gửi OTP qua Firebase Phone Auth → Nhập OTP → Login → Main Screen

3. **Login Flow:**
   - Nhập email + password → Firebase verify
   - Nếu chưa verify → Navigate → Email Verification Screen
   - Nếu đã verify → Main Screen

## 📚 Code Examples

### Sử dụng ViewModel

```kotlin
private val viewModel: FirebaseAuthViewModel by activityViewModels {
    FirebaseAuthViewModelFactory(
        FirebaseAuthRepository()
    )
}

// Observe state
viewLifecycleOwner.lifecycleScope.launch {
    viewModel.uiState.collect { state ->
        when (state) {
            is AuthUiState.Loading -> showLoading(true)
            is AuthUiState.LoginSuccess -> navigateToMain()
            is AuthUiState.EmailNotVerified -> navigateToEmailVerification()
            is AuthUiState.Error -> showError(state.message)
            else -> {}
        }
    }
}
```

### Register

```kotlin
viewModel.register(email, password, fullName)
// Firebase tự động:
// 1. Tạo user
// 2. Gửi email verification
// 3. Update display name
```

### Login

```kotlin
viewModel.login(email, password)
// Firebase tự động verify credentials
```

### Check Email Verification

```kotlin
viewModel.checkEmailVerification()
// Reload user và check isEmailVerified
```

### Get Current User

```kotlin
val user = FirebaseAuth.getInstance().currentUser
// Firebase tự quản lý user state
```

## 🎨 UI Components

### Material Design 3 Components
- `TextInputLayout` với `TextInputEditText` - Input fields
- `MaterialButton` - Buttons
- `ProgressBar` - Loading indicator
- `RadioGroup` với `RadioButton` - Selection

### Colors & Themes
- Primary: `#1E88E5` (Blue)
- Accent: `#00ACC1` (Cyan)
- Background: `#F5F5F5` (Light Gray)
- Text: `#212121` (Dark Gray)

## 🐛 Troubleshooting

### Build Errors
- **Gradle sync failed**: Kiểm tra internet connection, thử "Invalidate Caches / Restart"
- **Missing dependencies**: Chạy `./gradlew clean build`
- **google-services.json not found**: Đảm bảo file ở đúng vị trí `android/app/google-services.json`

### Firebase Errors
- **Default FirebaseApp is not initialized**: Kiểm tra `google-services.json` và sync Gradle
- **Email already in use**: Email đã được đăng ký trong Firebase
- **Email verification not received**: Check Spam folder, resend email từ app

### Runtime Errors
- **Authentication failed**: Kiểm tra email/password đúng chưa
- **Email not verified**: User cần verify email trước khi login

## 📝 TODO

- [ ] Implement Main Screen UI (Booking features)
- [ ] Add Forgot Password flow (Firebase có sẵn)
- [ ] Add Phone Authentication (Firebase Phone Auth)
- [ ] Add Biometric authentication
- [ ] Add Dark theme support
- [ ] Add Firestore để lưu user data
- [ ] Add Unit tests
- [ ] Add UI tests với Espresso

## 📄 License

Proprietary - La Vela Saigon Hotel

## 📚 Tài Liệu Tham Khảo

- `FIREBASE_SETUP_INSTRUCTIONS.md` - Hướng dẫn setup Firebase (ở root project)
- `FIREBASE_MIGRATION_COMPLETE.md` - Chi tiết migration sang Firebase
- Firebase Console: https://console.firebase.google.com
- Firebase Auth Docs: https://firebase.google.com/docs/auth

## 👥 Support

Liên hệ đội phát triển để được hỗ trợ.
