# VAULTPOOL - Monorepo Architecture

## Tổng Quan

Dự án VAULTPOOL sử dụng cấu trúc **Monorepo** kết hợp:
- **Frontend**: Android App (Java)
- **Backend**: Spring Boot Services (Java 17)
- **Shared Libraries**: Code dùng chung

```
JAVA-VAULTPOOL/
├── apps/                    # Android Applications
│   └── android-customer/    # Customer App (Android - Java)
│
├── backends/                # Backend Services
│   └── auth-service/        # Auth Service (Spring Boot)
│
├── libraries/               # Shared Libraries
│   └── common-models/       # Shared DTOs/Entities
│
├── gradle/                  # Gradle Wrapper
├── build.gradle.kts         # Root build config
├── settings.gradle.kts      # Project settings
└── gradle.properties       # Gradle properties
```

---

## Cấu Trúc Thư Mục Chi Tiết

```
JAVA-VAULTPOOL/
│
├── settings.gradle.kts                 # Cấu hình include modules
├── build.gradle.kts                    # Root build configuration
├── gradle.properties                   # Gradle properties
├── libs.versions.toml                 # Version catalog
│
├── gradle/                           # Gradle wrapper
│   └── wrapper/
│
├── apps/                             # === FRONTEND (Android) ===
│   └── android-customer/
│       ├── build.gradle.kts
│       ├── src/main/
│       │   ├── AndroidManifest.xml
│       │   ├── java/com/vaultpool/customer/
│       │   │   ├── VaultPoolApplication.java
│       │   │   │
│       │   │   ├── di/                     # Dependency Injection
│       │   │   │   ├── AppModule.java
│       │   │   │   └── ViewModelModule.java
│       │   │   │
│       │   │   ├── domain/                  # DOMAIN LAYER
│       │   │   │   ├── model/
│       │   │   │   │   ├── User.java
│       │   │   │   │   ├── Booking.java
│       │   │   │   │   └── Result.java
│       │   │   │   │
│       │   │   │   ├── repository/
│       │   │   │   │   ├── AuthRepository.java      # Interface
│       │   │   │   └── BookingRepository.java      # Interface
│       │   │   │   │
│       │   │   │   └── usecase/
│       │   │   │       ├── LoginUseCase.java
│       │   │   │       ├── RegisterUseCase.java
│       │   │   │       └── GetProfileUseCase.java
│       │   │   │
│       │   │   ├── data/                     # DATA LAYER
│       │   │   │   ├── remote/
│       │   │   │   │   ├── api/
│       │   │   │   │   │   ├── AuthApi.java
│       │   │   │   │   │   └── BookingApi.java
│       │   │   │   │   ├── dto/
│       │   │   │   │   │   ├── LoginRequestDto.java
│       │   │   │   │   │   ├── LoginResponseDto.java
│       │   │   │   │   │   └── UserDto.java
│       │   │   │   │   └── interceptor/
│       │   │   │   │       └── AuthInterceptor.java
│       │   │   │   │
│       │   │   │   ├── local/
│       │   │   │   │   ├── db/
│       │   │   │   │   │   ├── AppDatabase.java
│       │   │   │   │   │   ├── dao/
│       │   │   │   │   │   └── entity/
│       │   │   │   │   │       └── UserEntity.java
│       │   │   │   │   └── prefs/
│       │   │   │   │       └── PreferencesManager.java
│       │   │   │   │
│       │   │   │   ├── repository/
│       │   │   │   │   ├── AuthRepositoryImpl.java
│       │   │   │   │   └── BookingRepositoryImpl.java
│       │   │   │   │
│       │   │   │   └── mapper/
│       │   │   │       ├── UserMapper.java
│       │   │   │       └── BookingMapper.java
│       │   │   │
│       │   │   └── presentation/            # PRESENTATION LAYER
│       │   │       ├── ui/
│       │   │       │   ├── auth/
│       │   │       │   │   ├── AuthActivity.java
│       │   │       │   │   ├── LoginFragment.java
│       │   │       │   │   ├── RegisterFragment.java
│       │   │       │   │   ├── AuthViewModel.java
│       │   │       │   │   └── AuthViewModelFactory.java
│       │   │       │   │
│       │   │       │   ├── main/
│       │   │       │   │   ├── MainActivity.java
│       │   │       │   │   └── MainViewModel.java
│       │   │       │   │
│       │   │       │   └── common/
│       │   │       │       ├── BaseFragment.java
│       │   │       │       └── BaseViewModel.java
│       │   │       │
│       │   │       ├── navigation/
│       │   │       │   ├── NavGraph.java
│       │   │       │   └── Screen.java
│       │   │       │
│       │   │       └── state/
│       │   │           ├── AuthUiState.java
│       │   │           └── UiEvent.java
│       │   │
│       │   │   └── util/                     # Utilities
│       │   │       ├── NetworkUtils.java
│       │   │       ├── Extensions.java
│       │   │       └── Constants.java
│       │   │
│       │   └── res/                     # Resources
│       │       ├── layout/
│       │       ├── values/
│       │       ├── drawable/
│       │       └── navigation/
│       │
│       └── proguard-rules.pro
│
├── backends/                          # === BACKEND (Spring Boot) ===
│   └── auth-service/
│       ├── pom.xml
│       ├── src/main/
│       │   ├── java/com/vaultpool/auth/
│       │   │   ├── AuthApplication.java
│       │   │   │
│       │   │   ├── config/
│       │   │   │   ├── SecurityConfig.java
│       │   │   │   ├── FirebaseConfig.java
│       │   │   │   ├── OpenApiConfig.java
│       │   │   │   └── RedisConfig.java
│       │   │   │
│       │   │   ├── controller/
│       │   │   │   ├── AuthController.java
│       │   │   │   └── UserController.java
│       │   │   │
│       │   │   ├── service/
│       │   │   │   ├── AuthService.java
│       │   │   │   ├── UserService.java
│       │   │   │   └── TokenService.java
│       │   │   │
│       │   │   ├── repository/
│       │   │   │   ├── UserRepository.java
│       │   │   │   └── TokenRepository.java
│       │   │   │
│       │   │   ├── domain/
│       │   │   │   ├── entity/
│       │   │   │   │   └── User.java
│       │   │   │   ├── enums/
│       │   │   │   │   └── UserRole.java
│       │   │   │   └── mapper/
│       │   │   │       └── UserMapper.java
│       │   │   │
│       │   │   ├── dto/
│       │   │   │   ├── request/
│       │   │   │   │   ├── RegisterRequest.java
│       │   │   │   │   └── LoginRequest.java
│       │   │   │   └── response/
│       │   │   │       ├── ApiResponse.java
│       │   │   │       ├── UserResponse.java
│       │   │   │       └── LoginResponse.java
│       │   │   │
│       │   │   ├── security/
│       │   │   │   ├── FirebaseTokenFilter.java
│       │   │   │   ├── JwtTokenProvider.java
│       │   │   │   └── UserPrincipal.java
│       │   │   │
│       │   │   └── exception/
│       │   │       ├── GlobalExceptionHandler.java
│       │   │       ├── AuthException.java
│       │   │       └── ResourceNotFoundException.java
│       │   │
│       │   └── resources/
│       │       ├── application.yml
│       │       └── application-dev.yml
│       │
│       └── src/test/
│           └── java/com/vaultpool/auth/
│
└── libraries/                        # === SHARED LIBRARIES ===
    └── common-models/
        ├── build.gradle.kts
        └── src/main/java/com/vaultpool/models/
            ├── User.java
            ├── Booking.java
            ├── Slot.java
            ├── ApiResponse.java
            └── Result.java
```

---

## Authentication Flow (Luồng Xác Thực)

```
┌──────────────────────────────────────────────────────────────────────────────┐
│                         AUTHENTICATION FLOW                                   │
└──────────────────────────────────────────────────────────────────────────────┘

    ┌─────────────┐         ┌─────────────┐         ┌─────────────────────┐
    │   Android  │         │   Backend   │         │      Firebase       │
    │    App     │         │ Auth Service│         │    Authentication   │
    └──────┬──────┘         └──────┬──────┘         └──────────┬──────────┘
           │                       │                         │
           │  1. User enters      │                         │
           │  email/password       │                         │
           │◄──────────────────────│                         │
           │                       │                         │
           │                       │   2. Sign in with       │
           │                       │   Firebase Auth         │
           │──────────────────────┼────────────────────────►│
           │                       │                         │
           │                       │   3. Return Firebase   │
           │                       │   ID Token              │
           │◄──────────────────────┼─────────────────────────│
           │                       │                         │
           │  4. Send token to     │                         │
           │  Backend /api/auth/me │                         │
           │──────────────────────►│                         │
           │                       │                         │
           │                       │  5. Verify Firebase ID  │
           │                       │  Token                  │
           │                       │────────────────────────►│
           │                       │                         │
           │                       │  6. Return verified     │
           │                       │  user info              │
           │◄──────────────────────┤                         │
           │                       │                         │
           │  7. Allow access to   │                         │
           │  protected features   │                         │
           │                       │                         │
```

### Chi Tiết Từng Bước

| Bước | Mô Tả | Code |
|------|-------|------|
| 1 | User nhập email/password trên Android App | `LoginFragment.java` |
| 2 | App gọi Firebase Auth để xác thực | `FirebaseAuthRepository.loginWithEmail()` |
| 3 | Firebase trả về Firebase ID Token | `FirebaseUser.getIdToken()` |
| 4 | App gửi token lên Backend | `AuthApi.me(token)` |
| 5 | Backend verify token với Firebase | `FirebaseTokenFilter.doFilter()` |
| 6 | Backend trả về user info từ DB | `AuthController.me()` |
| 7 | App lưu session và cho phép truy cập | `MainActivity` |

---

## Cấu Trúc Dữ Liệu (Entity Relationship)

### User Entity

```java
// Backend - User Entity
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "firebase_uid", nullable = false, unique = true)
    private String firebaseUid;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "phone")
    private String phone;

    @Column(name = "status")
    @Builder.Default
    private String status = "ACTIVE";

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles")
    @Enumerated(EnumType.STRING)
    private Set<UserRole> roles;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
```

### UserRole Enum

```java
public enum UserRole {
    USER,       // Khách hàng
    STAFF,      // Nhân viên
    ADMIN       // Quản lý
}
```

---

## Công Nghệ Sử Dụng

### Frontend (Android - Java)

| Category | Technology | Version |
|----------|------------|---------|
| Language | Java | 17 |
| Build Tool | Gradle | 8.2.x |
| AGP | Android Gradle Plugin | 8.2.x |
| Min SDK | API 26 (Android 8.0) | - |
| Target SDK | API 34 (Android 14) | - |

| Category | Library | Purpose |
|----------|---------|---------|
| DI | Hilt 2.50 | Dependency Injection |
| Architecture | MVVM + Clean Architecture | Separation of concerns |
| State | LiveData | Reactive UI updates |
| Navigation | Navigation Component | Fragment navigation |
| Network | Retrofit 2.9 | REST API calls |
| JSON | Gson 2.10 | JSON parsing |
| Database | Room 2.6 | Local SQLite |
| Auth | Firebase Auth 22 | User authentication |
| Security | EncryptedSharedPrefs | Secure token storage |
| Async | RxJava 3 | Asynchronous operations |

### Backend (Spring Boot - Java)

| Category | Technology | Version |
|----------|------------|---------|
| Language | Java | 17 LTS |
| Framework | Spring Boot | 3.2.x |
| Build Tool | Maven | 3.9.x |

| Category | Library | Purpose |
|----------|---------|---------|
| Web | spring-boot-starter-web | REST APIs |
| Security | spring-boot-starter-security | Authentication |
| Data JPA | spring-boot-starter-data-jpa | Database access |
| Validation | spring-boot-starter-validation | Bean validation |
| Database | MySQL 8.x / PostgreSQL | Primary DB |
| Cache | Redis | Session caching |
| Firebase | firebase-admin 9.x | Token verification |
| JWT | jjwt 0.12.x | Token management |
| Documentation | springdoc-openapi | Swagger UI |

---

## Các Layer Trong Architecture

### 1. PRESENTATION LAYER (Android)

```
Responsibility: Hiển thị UI và nhận tương tác từ user

Components:
├── Activity/Fragment     # View - Hiển thị UI
├── ViewModel             # Xử lý logic và state
└── UiState               # Trạng thái UI
```

**Ví dụ:**
```java
// LoginFragment.java - View
public class LoginFragment extends Fragment {
    private void setupClickListeners() {
        binding.btnLogin.setOnClickListener(v -> {
            String email = binding.etEmail.getText().toString();
            String password = binding.etPassword.getText().toString();
            viewModel.login(email, password);
        });
    }
}

// AuthViewModel.java - ViewModel
public class AuthViewModel extends ViewModel {
    private final MutableLiveData<AuthUiState> uiState = new MutableLiveData<>();

    public void login(String email, String password) {
        uiState.setValue(AuthUiState.loading());
        loginUseCase.execute(email, password)
            .subscribe(result -> {
                uiState.setValue(AuthUiState.success(result));
            });
    }
}
```

### 2. DOMAIN LAYER (Android)

```
Responsibility: Business logic - Độc lập với data source

Components:
├── Model          # Business entities
├── Repository    # Interface (Abstract)
└── UseCase       # Business rules
```

**Ví dụ:**
```java
// AuthRepository.java - Interface
public interface AuthRepository {
    Single<Result<User>> login(String email, String password);
    Single<Result<User>> register(String email, String password, String name);
    Single<Result<User>> getProfile(String token);
}

// LoginUseCase.java - Business Logic
public class LoginUseCase {
    public Single<Result<User>> execute(String email, String password) {
        if (email.isEmpty()) {
            return Single.just(Result.failure(new ValidationException("Email required")));
        }
        return authRepository.login(email, password);
    }
}
```

### 3. DATA LAYER (Android)

```
Responsibility: Truy cập data từ external sources

Components:
├── remote/           # API calls (Retrofit)
│   ├── api/          # API interfaces
│   ├── dto/          # Data Transfer Objects
│   └── interceptor/  # HTTP interceptors
│
├── local/            # Local storage
│   ├── db/           # Room database
│   └── prefs/        # SharedPreferences
│
├── repository/       # Repository implementations
└── mapper/           # DTO to Domain mappers
```

**Ví dụ:**
```java
// AuthRepositoryImpl.java
public class AuthRepositoryImpl implements AuthRepository {
    private final AuthApi authApi;
    private final TokenManager tokenManager;

    @Override
    public Single<Result<User>> login(String email, String password) {
        // 1. Get Firebase ID Token
        // 2. Call Backend API
        // 3. Save token
        // 4. Return Result
    }
}
```

### 4. BACKEND LAYER (Spring Boot)

```
Responsibility: Xử lý business logic phía server

Components:
├── controller/       # REST endpoints
├── service/          # Business logic
├── repository/       # Database access
├── domain/          # Entities & enums
├── dto/             # Request/Response DTOs
├── security/        # Authentication filters
└── exception/      # Error handling
```

**Ví dụ:**
```java
// AuthController.java
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @PostMapping("/register")
    public ApiResponse<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.ok(authService.register(request));
    }
}

// AuthService.java
@Service
public class AuthService {
    public UserResponse register(RegisterRequest request) {
        // 1. Verify Firebase token
        FirebaseToken token = FirebaseAuth.getInstance().verifyIdToken(request.getToken());
        // 2. Check if user exists
        // 3. Create user in DB
        // 4. Return response
    }
}
```

---

## Hướng Dẫn Phát Triển

### Quy Tắc Đặt Tên

| Loại | Quy Tắc | Ví dụ |
|------|---------|--------|
| Class | PascalCase | `LoginFragment`, `AuthViewModel` |
| Method | camelCase | `login()`, `getProfile()` |
| Variable | camelCase | `userName`, `authToken` |
| Package | lowercase | `com.vaultpool.customer` |
| Constant | UPPER_SNAKE_CASE | `BASE_URL`, `DEFAULT_TIMEOUT` |

### Cấu Trúc Package

```
com.vaultpool.{app_name}/
├── di/                    # Dependency Injection (Hilt modules)
├── domain/                # Business logic layer
│   ├── model/            # Domain entities
│   ├── repository/       # Repository interfaces
│   └── usecase/         # Use cases
├── data/                 # Data layer
│   ├── remote/          # API calls
│   ├── local/           # Local storage
│   ├── repository/      # Repository implementations
│   └── mapper/          # Object mappers
├── presentation/        # UI layer
│   ├── ui/              # Activities, Fragments, ViewModels
│   ├── navigation/      # Navigation setup
│   └── state/           # UI state classes
└── util/                # Utilities
```

### Quy Tắc Code

1. **Single Responsibility**: Mỗi class chỉ có một trách nhiệm
2. **Dependency Injection**: Sử dụng Hilt để quản lý dependencies
3. **Repository Pattern**: Luôn qua interface cho repository
4. **UseCase cho Business Logic**: Logic nghiệp vụ đặt trong UseCase
5. **Immutable State**: UiState nên là immutable
6. **Error Handling**: Xử lý lỗi tập trung

---

## API Endpoints

### Auth Service

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|----------------|
| POST | `/api/auth/register` | Register new user | No |
| GET | `/api/auth/me` | Get current user profile | Yes (Bearer Token) |
| POST | `/api/auth/refresh` | Refresh token | Yes |

### Response Format

```json
// Success
{
    "success": true,
    "message": "Operation successful",
    "data": {
        "id": 1,
        "email": "user@example.com",
        "fullName": "John Doe"
    }
}

// Error
{
    "success": false,
    "message": "Error message",
    "errors": []
}
```

---

## Cấu Hình Môi Trường

### Backend - application.yml

```yaml
server:
  port: 8081

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/vaultpool
    username: root
    password: ${DB_PASSWORD}
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true

firebase:
  credentials:
    path: ${FIREBASE_CREDENTIALS_PATH}

jwt:
  secret: ${JWT_SECRET}
  expiration: 86400000
```

### Android - BuildConfig

```java
public class BuildConfig {
    public static final String BASE_URL = "http://10.0.2.2:8081/";  // Emulator
    // public static final String BASE_URL = "http://<IP>:8081/";    // Real device
}
```

---

## Testing

### Unit Test Structure

```
src/test/java/com/vaultpool/
├── domain/
│   └── usecase/
│       └── LoginUseCaseTest.java
├── data/
│   └── repository/
│       └── AuthRepositoryImplTest.java
└── presentation/
    └── ui/
        └── auth/
            └── AuthViewModelTest.java
```

### Integration Test

```java
@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Test
    void register_withValidToken_shouldReturn201() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validRegisterRequest))
                .andExpect(status().isCreated());
    }
}
```

---

## Git Branching Strategy

```
main
│
├── develop
│   │
│   ├── feature/authentication
│   │   ├── feature/auth-android
│   │   └── feature/auth-backend
│   │
│   ├── feature/booking
│   │   ├── feature/booking-android
│   │   └── feature/booking-backend
│   │
│   └── bugfix/fix-login-issue
│
└── release/v1.0.0
```

### Branch Naming

- Feature: `feature/<feature-name>`
- Bugfix: `bugfix/<issue-description>`
- Hotfix: `hotfix/<critical-fix>`
- Release: `release/<version>`

---

## CI/CD Pipeline

```yaml
# GitHub Actions Example
name: Build and Test

on:
  push:
    branches: [main, develop]

jobs:
  build-android:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Setup JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
      - name: Build Android
        run: ./gradlew assembleDebug

  build-backend:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Build Backend
        run: cd backends/auth-service && mvn clean package
```

---

## Tài Liệu Tham Khảo

- [Android Architecture Components](https://developer.android.com/topic/libraries/architecture)
- [Hilt Dependency Injection](https://dagger.dev/hilt/)
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Firebase Authentication](https://firebase.google.com/docs/auth)
- [Clean Architecture](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)

---

## Liên Hệ & Hỗ Trợ

Nếu có câu hỏi về kiến trúc này, vui lòng:
1. Xem lại tài liệu này
2. Kiểm tra code mẫu trong các module
3. Liên hệ Tech Lead của dự án
