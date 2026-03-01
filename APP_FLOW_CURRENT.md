# 📱 Luồng Hoạt Động Hiện Tại Của Ứng Dụng *La Vela Pool Booking*

## 1. Khởi động ứng dụng

1. User mở app → Android chạy `SplashActivity` (khai báo trong `AndroidManifest.xml` làm `LAUNCHER`).
2. `LaVelaApplication` được tạo trước:
   - Gọi `FirebaseApp.initializeApp(this)` (nếu chưa init).
   - Lấy `FirebaseAuth.getInstance()` để kiểm tra kết nối Firebase.
3. `SplashActivity`:
   - Hiển thị màn hình splash (màu `primary`).
   - Dùng `lifecycleScope.launch { delay(2000); navigateToNextScreen() }` để chờ ~2 giây.
   - `navigateToNextScreen()`:
     - Lấy `FirebaseAuth.getInstance().currentUser`.
     - Nếu **currentUser != null** và `isEmailVerified == true` → chuyển sang `MainActivity`.
     - Ngược lại → chuyển sang `AuthActivity` (luồng đăng nhập/đăng ký).

Kết quả:  
→ Người đã đăng nhập + xác thực email sẽ vào thẳng màn hình chính.  
→ Người mới / chưa xác thực sẽ vào màn hình Auth.

---

## 2. Màn hình Auth (`AuthActivity`)

### 2.1. Layout & cấu trúc
- `ActivityAuthBinding` chứa:
  - Logo + tên app + subtitle.
  - `FragmentContainerView` bên dưới (bên trong `ScrollView` để cuộn được).
- Khi mở `AuthActivity`:
  - Nếu `savedInstanceState == null` → gắn `LoginFragment` vào `fragmentContainer`.

### 2.2. Điều hướng nội bộ
`AuthActivity` cung cấp các hàm:
- `navigateToRegister()` → thay `fragmentContainer` bằng `RegisterFragment`, `addToBackStack`.
- `navigateToLogin()` → thay bằng `LoginFragment`, `addToBackStack`.
- `navigateToEmailVerification(email)` → mở `EmailVerificationFragment`.
- `navigateToPhoneOtp(phone)` → mở `PhoneOtpVerificationFragment`.

---

## 3. Đăng nhập (`LoginFragment`)

### 3.1. Giao diện
- Ô nhập:
  - `Email hoặc số điện thoại` (hiện tại dùng như email string).
  - `Mật khẩu`.
- Nút:
  - **Đăng nhập**.
  - Link **Đăng ký** (chuyển sang `RegisterFragment`).
  - Link **Quên mật khẩu** (hiện mới chỉ hiển thị Toast).

### 3.2. Xử lý
1. Validate:
   - Bắt buộc nhập email/phone.
   - Bắt buộc mật khẩu ≥ 8 ký tự.
2. Nếu hợp lệ:
   - Gọi `viewModel.login(emailOrPhone, password)`.
3. `FirebaseAuthViewModel.login()`:
   - `_uiState = Loading`.
   - Gọi `FirebaseAuthRepository.loginWithEmail(email, password)`:
     - Dùng `FirebaseAuth.signInWithEmailAndPassword().await()`.
   - Nếu đăng nhập thành công:
     - Gọi `repository.reloadUser()` để lấy user mới nhất.
     - Nếu `isEmailVerified == true` → `_uiState = LoginSuccess(user)`.
     - Nếu chưa verify → `_uiState = EmailNotVerified(message, user)`.
   - Nếu lỗi → `_uiState = Error(message friendly)`.
4. `LoginFragment.observeViewModel()`:
   - `Loading` → hiển thị ProgressBar, disable nút.
   - `LoginSuccess` → Toast “Đăng nhập thành công” → gọi `navigateToMain()`:
     - Mở `MainActivity` với `FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_CLEAR_TASK`.
   - `EmailNotVerified` → Toast thông báo → gọi `AuthActivity.navigateToEmailVerification(user.email)`.
   - `Error` → hiển thị Toast lỗi.

---

## 4. Đăng ký (`RegisterFragment`)

### 4.1. Giao diện
- Các ô nhập:
  - Họ và tên.
  - Email.
  - Số điện thoại.
  - Mật khẩu.
  - Xác nhận mật khẩu.
- Nhóm lựa chọn phương thức xác thực:
  - RadioGroup `rgVerificationChannel`:
    - `rbEmail` – Xác thực qua email.
    - `rbSms` – OTP SMS (Firebase Phone Auth).
- Nút **Đăng ký**.
- Link **Đã có tài khoản? Đăng nhập** → `AuthActivity.navigateToLogin()`.
- ProgressBar cho trạng thái `Loading`.

### 4.2. Validate
`validateInput()` kiểm tra:
- Họ tên không rỗng.
- Email:
  - Không rỗng.
  - Đúng định dạng (`ValidationUtils.isValidEmail`).
- Số điện thoại:
  - Nếu không rỗng thì phải hợp lệ (`ValidationUtils.isValidPhone`).
- Mật khẩu:
  - Không rỗng.
  - Độ dài ≥ 6 ký tự (theo rule của Firebase).
- Xác nhận mật khẩu:
  - Không rỗng.
  - Trùng với mật khẩu.
- Bắt buộc **chọn một trong hai**: Email hoặc SMS trong `rgVerificationChannel`.

### 4.3. Hành vi khi nhấn Đăng ký

```kotlin
when (selectedChannelId) {
    R.id.rbEmail -> {
        viewModel.register(email, password, fullName)
    }
    R.id.rbSms -> {
        (activity as? AuthActivity)?.navigateToPhoneOtp(phone)
    }
}
```

#### 4.3.1. Trường hợp Email
1. Gọi `FirebaseAuthViewModel.register(email, password, fullName)`.
2. Trong ViewModel:
   - Gọi `FirebaseAuthRepository.registerWithEmail(...)`:
     - `createUserWithEmailAndPassword`.
     - Cập nhật `displayName = fullName`.
     - Gửi email xác thực `sendEmailVerification()`.
   - `onSuccess` → `_uiState = RegisterSuccess(message, user)`.
   - `onFailure` → `_uiState = Error(message friendly)`.
3. `RegisterFragment.observeViewModel()`:
   - `RegisterSuccess`:
     - Ẩn loading.
     - Toast: “Đăng ký thành công. Vui lòng kiểm tra email để xác thực.”
     - Gọi `AuthActivity.navigateToLogin()` để quay về màn hình đăng nhập.
     - Gọi `viewModel.resetState()` để clear state.

#### 4.3.2. Trường hợp SMS (Phone Auth)
1. Sau khi validate, nếu chọn `SMS`:
   - Lấy số điện thoại (cần ở dạng `+84xxxxxxxxx`).
   - Gọi `AuthActivity.navigateToPhoneOtp(phone)`.
2. `AuthActivity` thay fragment hiện tại bằng `PhoneOtpVerificationFragment`.

---

## 5. Xác thực Email (`EmailVerificationFragment`)

### 5.1. Giao diện
- Hiển thị email cần xác thực.
- Nút:
  - **Gửi lại email xác thực**.
  - **Kiểm tra trạng thái xác thực**.

### 5.2. Luồng xử lý
- `resendVerificationEmail()`:
  - Gọi `FirebaseAuthRepository.resendVerificationEmail()` → `user.sendEmailVerification()`.
  - Thành công → `_uiState = EmailSent(message)`.
- `checkEmailVerification()`:
  - Gọi `reloadUser()`:
    - Nếu `isEmailVerified == true` → `_uiState = EmailVerified(user)`.
    - Ngược lại → `_uiState = EmailNotVerified(message, user)`.
- Fragment quan sát `uiState`:
  - `EmailSent` → Toast “Email xác thực đã được gửi...”.
  - `EmailVerified` → Toast thành công → `navigateToMain()`.
  - `EmailNotVerified`/`Error` → hiển thị Toast tương ứng.

---

## 6. Xác thực SMS OTP (`PhoneOtpVerificationFragment`)

### 6.1. Giao diện
- Text hiển thị số điện thoại.
- Ô nhập mã OTP (6 số).
- Nút:
  - **Xác thực**.
  - **Gửi lại mã OTP**.
- ProgressBar cho trạng thái đang gửi/xác thực.

### 6.2. Gửi mã OTP
- Khi fragment được tạo:
  - Lấy `phoneNumber` từ `arguments`.
  - Gọi `startPhoneNumberVerification(phoneNumber)`:
    - Tạo `PhoneAuthOptions` với:
      - `setPhoneNumber(phoneNumber)` – dạng `+84...`.
      - `setTimeout(60s)`.
      - `setActivity(requireActivity())`.
      - `setCallbacks(callbacks)`.
    - Gọi `PhoneAuthProvider.verifyPhoneNumber(options)`.
- `callbacks`:
  - `onCodeSent(verificationId, token)`:
    - Lưu `verificationId` và `resendToken`.
    - Hiện Toast “Mã OTP đã được gửi.”
  - `onVerificationCompleted(credential)`:
    - Firebase có thể auto lấy mã từ SMS → gọi `signInWithPhoneAuthCredential(credential)`.
  - `onVerificationFailed(e)`:
    - Hiện Toast lỗi.

### 6.3. Xác thực OTP
- User nhập mã OTP và nhấn **Xác thực**:
  - Tạo `PhoneAuthCredential` từ `verificationId` + `code`.
  - Gọi `signInWithCredential(credential)`:
    - Thành công → điều hướng tới `MainActivity` và clear back stack.
    - Thất bại → Toast lỗi.
- Nút **Gửi lại mã**:
  - Dùng lại `resendToken` với `PhoneAuthProvider.verifyPhoneNumber` để gửi lại.

---

## 7. Màn hình chính (`MainActivity`)

### 7.1. Giao diện
- Text `tvWelcome`: “Chào mừng đến với La Vela Pool Booking!”.
- Nút `btnLogout`: “Đăng xuất”.

### 7.2. Hành vi
- Khi nhấn **Đăng xuất**:

```kotlin
FirebaseAuth.getInstance().signOut()
val intent = Intent(this, AuthActivity::class.java).apply {
    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
}
startActivity(intent)
finish()
```

→ Xóa session Firebase hiện tại, quay về luồng Auth (login/register).

---

## 8. Tóm tắt luồng tổng thể

1. **SplashActivity**
   - Nếu user đã login + verify email → `MainActivity`.
   - Ngược lại → `AuthActivity`.
2. **AuthActivity**
   - Mặc định hiển thị `LoginFragment`.
3. **LoginFragment**
   - Đăng nhập Email/Password.
   - Nếu email verified → `MainActivity`.
   - Nếu chưa verified → `EmailVerificationFragment`.
4. **RegisterFragment**
   - Đăng ký Email/Password.
   - Sau thành công → quay về `LoginFragment`.
   - Nếu chọn SMS → sang `PhoneOtpVerificationFragment` (Phone Auth).
5. **EmailVerificationFragment**
   - Gửi lại mail, kiểm tra verify → `MainActivity`.
6. **PhoneOtpVerificationFragment**
   - Gửi/nhập OTP SMS → `MainActivity`.
7. **MainActivity**
   - Hiển thị màn hình chính.
   - Nút Đăng xuất → quay lại `AuthActivity` (login/register).

Luồng này bao phủ cả **Email/Password + Email verification** và **Phone OTP (Firebase Phone Auth)**, đồng thời có Splash, Auth container, và Logout rõ ràng. 

