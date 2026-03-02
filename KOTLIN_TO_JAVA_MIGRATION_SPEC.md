## 🎯 Mục tiêu tài liệu

- **Mục đích**: Làm “đề bài cứng” cho AI/Dev khi chuyển **toàn bộ mã Kotlin hiện tại sang Java** trong project Android `android/`.
- **Yêu cầu quan trọng**:
  - **Không thay đổi nghiệp vụ**: Login, Register, Email Verification, Phone OTP (Firebase Phone Auth) phải hoạt động **y hệt**.
  - **Giữ nguyên UI/UX & layout XML**: Không sửa `res/layout` trừ khi bắt buộc do Java yêu cầu.
  - **Giữ nguyên kiến trúc**: Splash → Auth container → Fragments → Main, sử dụng Firebase Auth.
  - **Tập trung duy nhất vào việc đổi ngôn ngữ Kotlin → Java**, không thêm tính năng mới, không refactor kiến trúc.

---

## 1. Phạm vi migration

### 1.1. Thư mục cần chuyển từ Kotlin sang Java

Trong `android/app/src/main/java/com/lavelahotel/poolbooking/`:

- `LaVelaApplication.kt`
- `data/repository/FirebaseAuthRepository.kt`
- `ui/auth/AuthActivity.kt`
- `ui/auth/LoginFragment.kt`
- `ui/auth/RegisterFragment.kt`
- `ui/auth/EmailVerificationFragment.kt`
- `ui/auth/PhoneOtpVerificationFragment.kt`
- `ui/auth/FirebaseAuthViewModel.kt`
- `ui/auth/FirebaseAuthViewModelFactory.kt`
- `ui/splash/SplashActivity.kt`
- `ui/main/MainActivity.kt`
- `util/ValidationUtils.kt`

**Yêu cầu**:
- Sau migration, **tất cả file trên phải tồn tại bản Java tương đương**, cùng package, cùng tên class (trừ phần `.kt` → `.java`).
- Có thể giữ file Kotlin song song trong quá trình chuyển đổi, nhưng **kết quả cuối cùng** nên là:
  - Hoặc chỉ còn Java.
  - Hoặc Java là “source of truth” và Kotlin không được dùng trong code chạy.

### 1.2. Thư mục KHÔNG chỉnh

- `res/layout/**`
- `res/values/**`
- `res/drawable/**`
- `AndroidManifest.xml`
- `build.gradle.kts` (project và app level)  

Chỉ sửa Manifest nếu Java bắt buộc đổi tên Activity/Fragment (không khuyến khích).

---

## 2. Yêu cầu chung khi convert Kotlin → Java

- **Giữ nguyên tên package**:
  - Ví dụ: `package com.lavelahotel.poolbooking.ui.auth` → giữ nguyên trong file Java.
- **Giữ nguyên tên class, function, tham số public**:
  - Để không phải sửa lại XML (onClick), Navigation, hoặc chỗ gọi khác.
- **Giữ nguyên logic**:
  - Flow của Firebase Auth, xử lý lỗi, text message phải giống 100%.
- **Không thay đổi string literal nghiệp vụ** (tiếng Việt hiển thị cho user).
- **Không xóa log / toast quan trọng** trừ khi thực sự dư thừa.

---

## 3. Chuyển ViewModel & StateFlow sang Java

### 3.1. Kotlin hiện tại (rút gọn)

File: `ui/auth/FirebaseAuthViewModel.kt`

```kotlin
class FirebaseAuthViewModel(
    private val repository: FirebaseAuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun register(email: String, password: String, fullName: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            repository.registerWithEmail(email, password, fullName)
                .onSuccess { user ->
                    _uiState.value = AuthUiState.RegisterSuccess(
                        message = "Đăng ký thành công. Vui lòng kiểm tra email để xác thực.",
                        user = user
                    )
                }
                .onFailure {
                    _uiState.value = AuthUiState.Error(getErrorMessage(it))
                }
        }
    }

    // login, resendVerificationEmail, checkEmailVerification, resetState...
}

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    data class RegisterSuccess(val message: String, val user: FirebaseUser) : AuthUiState()
    data class LoginSuccess(val user: FirebaseUser) : AuthUiState()
    data class EmailNotVerified(val message: String, val user: FirebaseUser) : AuthUiState()
    data class EmailVerified(val user: FirebaseUser) : AuthUiState()
    data class EmailSent(val message: String) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}
```

### 3.2. Yêu cầu khi chuyển sang Java

1. **Giữ ViewModel**:  
   - Class vẫn là `FirebaseAuthViewModel extends ViewModel`.
   - Có constructor nhận `FirebaseAuthRepository repository`.
2. **StateFlow thay bằng LiveData hoặc custom listener**:
   - **Ưu tiên**: dùng `MutableLiveData<AuthUiState>`:
     - `private MutableLiveData<AuthUiState> uiState = new MutableLiveData<>(AuthUiState.Idle.INSTANCE);`
     - Public getter: `LiveData<AuthUiState> getUiState()`.
   - Toàn bộ fragment hiện đang `collect` `StateFlow` → cần chuyển thành `observe()` LiveData.
3. **Coroutines + `viewModelScope`**:
   - Không dùng coroutines trong Java.
   - Dùng callback của Firebase trực tiếp trong ViewModel hoặc Repository.
   - Có thể:
     - Giữ logic async trong `FirebaseAuthRepository` (Java) và ViewModel chỉ wrap callback.
4. **Sealed class AuthUiState**:
   - Trong Java, sealed class có thể mô phỏng bằng:
     - `abstract class AuthUiState {}` + các subclass static:
       - `public static final class Idle extends AuthUiState { public static final Idle INSTANCE = new Idle(); }`
       - `public static final class Loading extends AuthUiState { public static final Loading INSTANCE = new Loading(); }`
       - Các class có field (`RegisterSuccess`, `Error`…) dùng final field + constructor.

### 3.3. Mẫu Java mong muốn (pseudo, không phải code hoàn chỉnh)

```java
public class FirebaseAuthViewModel extends ViewModel {

    private final FirebaseAuthRepository repository;
    private final MutableLiveData<AuthUiState> uiState = new MutableLiveData<>(AuthUiState.Idle.INSTANCE);

    public FirebaseAuthViewModel(FirebaseAuthRepository repository) {
        this.repository = repository;
    }

    public LiveData<AuthUiState> getUiState() {
        return uiState;
    }

    public void register(String email, String password, String fullName) {
        uiState.setValue(AuthUiState.Loading.INSTANCE);

        repository.registerWithEmail(email, password, fullName, new FirebaseAuthRepository.AuthCallback() {
            @Override
            public void onSuccess(FirebaseUser user) {
                uiState.postValue(new AuthUiState.RegisterSuccess(
                        "Đăng ký thành công. Vui lòng kiểm tra email để xác thực.",
                        user
                ));
            }

            @Override
            public void onError(Throwable t) {
                uiState.postValue(new AuthUiState.Error(getErrorMessage(t)));
            }
        });
    }

    // login, resendVerificationEmail, checkEmailVerification tương tự
}
```

`AuthUiState` trong Java:

```java
public abstract class AuthUiState {

    public static final class Idle extends AuthUiState {
        public static final Idle INSTANCE = new Idle();
        private Idle() {}
    }

    public static final class Loading extends AuthUiState {
        public static final Loading INSTANCE = new Loading();
        private Loading() {}
    }

    public static final class RegisterSuccess extends AuthUiState {
        public final String message;
        public final FirebaseUser user;
        public RegisterSuccess(String message, FirebaseUser user) {
            this.message = message;
            this.user = user;
        }
    }

    // LoginSuccess, EmailNotVerified, EmailVerified, EmailSent, Error tương tự
}
```

---

## 4. Chuyển FirebaseAuthRepository từ Kotlin sang Java

### 4.1. Yêu cầu

- Vẫn dùng `FirebaseAuth` của Firebase.
- Thay vì trả về `Result<FirebaseUser>` + `suspend fun`, dùng **callback interface** trong Java:

```java
public interface AuthCallback {
    void onSuccess(FirebaseUser user);
    void onError(Throwable t);
}
```

- Hàm Java tương đương:

```java
public void registerWithEmail(String email, String password, String fullName, AuthCallback callback);
public void loginWithEmail(String email, String password, AuthCallback callback);
public void reloadUser(AuthCallback callback);
public void resendVerificationEmail(SimpleCallback callback);
```

Trong đó `SimpleCallback`:

```java
public interface SimpleCallback {
    void onSuccess();
    void onError(Throwable t);
}
```

### 4.2. Logic giữ nguyên

- Tất cả mapping lỗi Firebase → message tiếng Việt (hiện trong `getErrorMessage` của ViewModel) **giữ nguyên nội dung**.
- Repository chỉ wrap Firebase SDK, **không đổi nghiệp vụ**:
  - `createUserWithEmailAndPassword` → update profile → `sendEmailVerification`.
  - `signInWithEmailAndPassword` → `reloadUser`.
  - `currentUser.sendEmailVerification()`.

---

## 5. Chuyển Fragment/Activity từ Kotlin sang Java

### 5.1. Nguyên tắc chung

- **Giữ nguyên layout XML + id**:
  - Nếu Kotlin đang dùng `FragmentLoginBinding`, Java có thể:
    - Dùng ViewBinding (nếu bật trong Gradle).
    - Hoặc dùng `findViewById` với đúng ID.
- **Giữ nguyên navigation logic**:
  - `AuthActivity` vẫn là container cho các fragment auth.
  - Các phương thức:
    - `navigateToRegister()`
    - `navigateToLogin()`
    - `navigateToEmailVerification(String email)`
    - `navigateToPhoneOtp(String phone)`
  - Cần tồn tại trong bản Java với cùng tên để không phải sửa các chỗ gọi.

### 5.2. LoginFragment / RegisterFragment

- Quan sát code Kotlin hiện tại:
  - Validate input bằng `ValidationUtils`.
  - Gọi `viewModel.register(...)`, `viewModel.login(...)`.
  - Collect `uiState` (StateFlow) và update UI (show/hide progress, Toast, navigate).

**Trong Java**:

- Dùng `FirebaseAuthViewModel` (Java) + `LiveData<AuthUiState>`:

```java
viewModel.getUiState().observe(getViewLifecycleOwner(), state -> {
    if (state instanceof AuthUiState.Loading) {
        showLoading(true);
    } else if (state instanceof AuthUiState.RegisterSuccess) {
        // ...
    }
    // các case khác tương tự Kotlin
});
```

- Phần validate input giữ nguyên điều kiện & message lỗi.

### 5.3. EmailVerificationFragment

- Kotlin hiện:
  - Hiển thị email.
  - Gọi `viewModel.resendVerificationEmail()` và `viewModel.checkEmailVerification()`.
  - Quan sát `AuthUiState.EmailSent`, `EmailVerified`, `EmailNotVerified`, `Error`.

- Java phải:
  - Giữ cùng flow.
  - Không đổi message Toast.

### 5.4. PhoneOtpVerificationFragment

- Kotlin hiện dùng `PhoneAuthProvider`, `PhoneAuthOptions`, callbacks:
  - `onCodeSent`
  - `onVerificationCompleted`
  - `onVerificationFailed`
  - `verifyPhoneNumber` + `resendToken`

**Trong Java**:

- API của Firebase Phone Auth là giống nhau.
- Yêu cầu:
  - Giữ logic: gửi OTP khi mở fragment, lưu `verificationId` & `resendToken`, verify OTP khi user nhập.
  - Khi xác thực thành công: chuyển đến `MainActivity` và clear back stack.

---

## 6. SplashActivity & MainActivity

### 6.1. SplashActivity

- Kotlin hiện:
  - Dùng `lifecycleScope.launch { delay(2000); navigateToNextScreen() }`.
  - Lấy `FirebaseAuth.getInstance().currentUser`.
  - Nếu user != null && `isEmailVerified` → `MainActivity`, else → `AuthActivity`.

**Trong Java**:

- Có thể dùng `Handler.postDelayed` hoặc `CountDownTimer`.
- Logic điều hướng phải giữ nguyên.

### 6.2. MainActivity

- Kotlin hiện:
  - Hiển thị text welcome.
  - Button Logout:
    - `FirebaseAuth.getInstance().signOut()`.
    - Intent → `AuthActivity` với flags `NEW_TASK | CLEAR_TASK`.

Java giữ nguyên luồng trên.

---

## 7. ValidationUtils

- Kotlin hiện:
  - `isValidEmail`, `isValidPhone`, `isValidPassword`… (suy luận theo usage).
- Java:

```java
public final class ValidationUtils {

    private ValidationUtils() {}

    public static boolean isValidEmail(String email) {
        // dùng Patterns.EMAIL_ADDRESS hoặc regex tương đương Kotlin
    }

    public static boolean isValidPhone(String phone) {
        // logic giống Kotlin: độ dài, bắt đầu bằng 0/84, v.v.
    }
}
```

Yêu cầu: **logic & điều kiện giống Kotlin**.

---

## 8. LaVelaApplication

- Kotlin hiện:
  - Extend `Application`.
  - Gọi `FirebaseApp.initializeApp(this)` (nếu cần).
  - Log debug.

- Java:

```java
public class LaVelaApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(this);
        // log tương đương nếu cần
    }
}
```

Manifest phải tiếp tục trỏ tới `com.lavelahotel.poolbooking.LaVelaApplication`.

---

## 9. Nguyên tắc kiểm tra sau khi migration

AI/Dev sau khi chuyển Kotlin → Java **bắt buộc** chạy qua các testcase thủ công sau:

1. **Đăng ký (Email)**:
   - Nhập họ tên, email mới, số điện thoại, mật khẩu, xác nhận.
   - Chọn Email verification.
   - Kỳ vọng:
     - Hiện Toast “Đăng ký thành công. Vui lòng kiểm tra email để xác thực.”
     - Firebase có user mới, email verification đã được gửi.
2. **Đăng nhập (Email chưa verify)**:
   - Dùng email ở bước 1, login.
   - Kỳ vọng:
     - Không vào Main.
     - Điều hướng đến EmailVerificationFragment/Activity.
     - Message giống bản Kotlin.
3. **Đăng nhập (Email đã verify)**:
   - Verify email qua link Firebase.
   - Login lại.
   - Kỳ vọng: Vào MainActivity, hiển thị text welcome, logout hoạt động.
4. **Phone OTP**:
   - Đăng ký chọn SMS, nhập số điện thoại test.
   - Kỳ vọng:
     - OTP được gửi (log/Toast).
     - Nhập đúng OTP → vào Main.
5. **Sai thông tin**:
   - Email format sai, mật khẩu < 6, email đã tồn tại…
   - Kỳ vọng: Message tiếng Việt giống `getErrorMessage` Kotlin ban đầu.

---

## 10. Tóm tắt yêu cầu cho AI thực hiện migration

1. **Không thiết kế lại hệ thống, chỉ đổi ngôn ngữ từ Kotlin sang Java**.
2. **Giữ nguyên nghiệp vụ, UI, string hiển thị, cấu trúc thư mục, Firebase setup**.
3. **Thay StateFlow + coroutines bằng LiveData + callback thuần Java**, nhưng vẫn giữ concept `AuthUiState`.
4. **Giữ ViewModel + Repository pattern**, không đẩy logic ngược về Activity/Fragment.
5. Sau khi xong, **build không lỗi**, app chạy qua được **tất cả flow auth hiện tại**:
   - Splash → Auth/Main
   - Register (Email & SMS)
   - Login
   - Email Verification
   - Phone OTP
   - Logout.

AI/Dev chỉ cần làm đúng tài liệu này, **không được “sáng tạo thêm kiến trúc mới”**, để đảm bảo tính ổn định và dễ bảo trì về lâu dài.

