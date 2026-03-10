# TÀI LIỆU KIẾN TRÚC HỆ THỐNG VAULTPOOL

## 1. TỔNG QUAN

### 1.1 Mục đích
Tài liệu này mô tả chi tiết kiến trúc hệ thống VAULTPOOL - một ứng dụng quản lý bể bơi với cấu trúc Monorepo kết hợp giữa Android App (Frontend) và Spring Boot (Backend). Tài liệu được thiết kế dành cho các thành viên trong đội nhóm, đặc biệt là những người chưa có kinh nghiệm phát triển Android, để hiểu và phát triển hệ thống một cách hiệu quả.

### 1.2 Phạm vi
Hệ thống VAULTPOOL bao gồm ba thành phần chính: ứng dụng Android cho khách hàng, dịch vụ xác thực (Authentication Service) phía backend, và các thư viện dùng chung. Toàn bộ hệ thống được quản lý trong một repository duy nhất (Monorepo) nhằm đảm bảo tính nhất quán về mã nguồn, quản lý dependencies, và quy trình CI/CD thống nhất.

### 1.3 Công nghệ chính
Phía frontend sử dụng Kotlin làm ngôn ngữ chính kết hợp với Android Jetpack Components bao gồm ViewModel, LiveData, Navigation Component, Room Database, và Hilt cho Dependency Injection. Phía backend sử dụng Java 17 với Spring Boot 3.2.x, Spring Security, Spring Data JPA, và MySQL/PostgreSQL làm cơ sở dữ liệu chính. Firebase Authentication được tích hợp để xử lý việc xác thực người dùng an toàn và tiện lợi.

---

## 2. CẤU TRÚC THƯ MỤC DỰ ÁN

### 2.1 Tổng quan cấu trúc Monorepo
Cấu trúc Monerepo của VAULTPOOL được tổ chức theo nguyên tắc phân chia rõ ràng giữa các thành phần. Thư mục gốc chứa các tệp cấu hình Gradle và Maven, trong đó `settings.gradle.kts` định nghĩa các module con, `build.gradle.kts` chứa cấu hình build chung, `gradle.properties` thiết lập các thuộc tính Gradle, và `gradle/libs.versions.toml` quản lý tập trung các phiên bản thư viện.

```
JAVA-VAULTPOOL/
├── settings.gradle.kts                 # Cấu hình include các module
├── build.gradle.kts                    # Cấu hình root build
├── gradle.properties                   # Thuộc tính Gradle
├── gradle/
│   ├── wrapper/                        # Gradle Wrapper
│   └── libs.versions.toml            # Version Catalog
│
├── apps/                              # === ỨNG DỤNG ANDROID ===
│   └── android-customer/
│       ├── build.gradle.kts
│       ├── src/main/
│       │   ├── AndroidManifest.xml
│       │   ├── java/com/vaultpool/customer/
│       │   │   ├── VaultPoolApplication.kt
│       │   │   ├── di/                     # Dependency Injection
│       │   │   ├── domain/                  # DOMAIN LAYER
│       │   │   ├── data/                    # DATA LAYER
│       │   │   └── presentation/            # PRESENTATION LAYER
│       │   └── res/                     # Resources
│       └── proguard-rules.pro
│
├── backends/                          # === DỊCH VỤ BACKEND ===
│   └── auth-service/
│       ├── pom.xml
│       ├── src/main/
│       │   ├── java/com/vaultpool/auth/
│       │   │   ├── AuthApplication.kt
│       │   │   ├── config/               # Cấu hình
│       │   │   ├── controller/          # REST Endpoints
│       │   │   ├── service/             # Business Logic
│       │   │   ├── repository/          # Database Access
│       │   │   ├── domain/              # Entities & Enums
│       │   │   ├── dto/                 # Data Transfer Objects
│       │   │   ├── security/            # Bảo mật
│       │   │   └── exception/           # Xử lý lỗi
│       │   └── resources/
│       │       └── application.yml
│       └── src/test/
│
└── libraries/                        # === THƯ VIỆN CHUNG ===
    └── common-models/
        ├── build.gradle.kts
        └── src/main/java/com/vaultpool/models/
```

### 2.2 Cấu trúc chi tiết Android App
Trong Android app, mã nguồn được tổ chức theo mô hình Clean Architecture với ba layer chính. Layer Domain chứa các đối tượng nghiệp vụ (model), các interface cho Repository, và các UseCase định nghĩa logic nghiệp vụ. Layer Data implement các interface từ Domain, quản lý việc gọi API qua Retrofit, lưu trữ cục bộ qua Room và SharedPreferences, và thực hiện ánh xạ giữa DTO và Domain model. Layer Presentation chịu trách nhiệm hiển thị giao diện thông qua Activities và Fragments, xử lý tương tác người dùng qua ViewModel, quản lý navigation, và quản lý trạng thái giao diện.

Cấu trúc chi tiết trong Android app bao gồm: thư mục `di/` chứa các module Hilt để cung cấp dependencies; thư mục `domain/model/` chứa các đối tượng nghiệp vụ như User, Booking, Result; thư mục `domain/repository/` định nghĩa các interface như AuthRepository, BookingRepository; thư mục `domain/usecase/` chứa các UseCase như LoginUseCase, RegisterUseCase; thư mục `data/remote/api/` định nghĩa các interface Retrofit; thư mục `data/remote/dto/` chứa các đối tượng truyền dữ liệu; thư mục `data/remote/interceptor/` chứa các interceptor cho HTTP; thư mục `data/local/` quản lý Room database và SharedPreferences; thư mục `data/repository/` implement các Repository; thư mục `data/mapper/` thực hiện ánh xạ DTO sang Domain model; thư mục `presentation/ui/` chứa Activities, Fragments, và ViewModels; thư mục `presentation/navigation/` cấu hình Navigation Component; và thư mục `presentation/state/` định nghĩa các trạng thái giao diện.

### 2.3 Cấu trúc chi tiết Backend Service
Backend service được xây dựng theo mô hình phân lớp truyền thống của Spring Boot. Thư mục `config/` chứa các cấu hình như SecurityConfig (thiết lập bảo mật), FirebaseConfig (cấu hình Firebase Admin), OpenApiConfig (Swagger API documentation), và RedisConfig (cấu hình Redis cache). Thư mục `controller/` chứa các REST Controllers định nghĩa các API endpoints. Thư mục `service/` chứa các Service classes xử lý logic nghiệp vụ. Thư mục `repository/` chứa các Repository interfaces để truy cập cơ sở dữ liệu. Thư mục `domain/` chứa các Entity và Enum. Thư mục `dto/` chứa các Data Transfer Objects cho request và response. Thư mục `security/` chứa các filter và provider bảo mật. Và thư mục `exception/` chứa các exception classes và global exception handler.

---

## 3. LUỒNG XÁC THỰC (AUTHENTICATION FLOW)

### 3.1 Sơ đồ luồng xác thực
Luồng xác thực trong hệ thống VAULTPOOL kết hợp giữa Firebase Authentication phía client và xác thực token phía server. Khi người dùng đăng nhập trên ứng dụng Android, hệ thống thực hiện các bước sau để đảm bảo tính bảo mật và trải người dùng mượt mà.

Đầu tiên, người dùng nhập email và mật khẩu vào giao diện đăng nhập. Fragment gọi phương thức login trên ViewModel, ViewModel kiểm tra tính hợp lệ của dữ liệu đầu vào theo các quy tắc đã định nghĩa trong LoginUseCase. Nếu dữ liệu hợp lệ, UseCase gọi phương thức loginWithEmail trên AuthRepository.

Tiếp theo, AuthRepository (FirebaseAuthRepository) gọi Firebase Authentication API để xác thực người dùng. Firebase trả về FirebaseUser nếu đăng nhập thành công, bao gồm các thông tin như UID, email, display name. Sau khi nhận được FirebaseUser, hệ thống gọi phương thức reloadUser để kiểm tra trạng thái xác thực email.

Nếu email đã được xác thực, hệ thống chuyển sang MainActivity và cho phép người dùng truy cập các tính năng chính. Nếu email chưa được xác thực, hệ thống chuyển sang màn hình EmailVerificationFragment để hướng dẫn người dùng xác thực email.

### 3.2 Xác thực với Backend
Đối với các API yêu cầu xác thực phía server, hệ thống sử dụng Firebase ID Token. Khi gọi các API như lấy thông tin profile người dùng, ứng dụng lấy Firebase ID Token từ FirebaseUser hiện tại và gửi kèm trong header Authorization dưới dạng Bearer Token.

Backend service nhận request, FirebaseTokenFilter trong Spring Security trích xuất token từ header, gọi Firebase Admin SDK để verify token, và tạo SecurityContext với thông tin người dùng. Controller nhận thông tin người dùng đã được xác thực thông qua annotation @AuthenticationPrincipal và xử lý yêu cầu.

---

## 4. CÁC LAYER TRONG KIẾN TRÚC

### 4.1 PRESENTATION LAYER (Android)
Layer này chịu trách nhiệm hiển thị giao diện người dùng và xử lý tương tác. Các thành phần chính bao gồm Activity và Fragment cho UI, ViewModel cho logic và trạng thái, State classes cho trạng thái giao diện, và Navigation Component cho việc điều hướng giữa các màn hình.

Trong Presentation Layer, ViewModel đóng vai trò trung tâm trong việc quản lý trạng thái giao diện. ViewModel chứa LiveData hoặc StateFlow để lưu trữ trạng thái hiện tại của màn hình, xử lý các sự kiện từ người dùng thông qua các phương thức, gọi UseCases để thực hiện logic nghiệp vụ, và cập nhật trạng thái để UI có thể observe và render tương ứng.

Ví dụ về AuthViewModel trong hệ thống: ViewModel này chứa MutableLiveData<AuthUiState> để lưu trữ trạng thái hiện tại của màn hình xác thực. Khi người dùng nhấn nút đăng nhập, phương thức login() được gọi, thiết lập trạng thái Loading, gọi LoginUseCase để thực hiện đăng nhập, và cập nhật trạng thái tương ứng với kết quả (LoginSuccess, EmailNotVerified, hoặc Error).

### 4.2 DOMAIN LAYER (Android)
Domain Layer đại diện cho lõi nghiệp vụ của ứng dụng, hoàn toàn độc lập với các implementation cụ thể. Layer này chứa các Domain Models là các đối tượng nghiệp vụ thuần túy như User, Booking, không phụ thuộc vào cách dữ liệu được lưu trữ hay truy xuất. Repository Interfaces định nghĩa các contract cho việc truy cập dữ liệu mà không quan tâm đến implementation cụ thể. UseCases đóng gói các nghiệp vụ nghiệp vụ, chứa logic nghiệp vụ và quy tắc nghiệp vụ.

UseCase là nơi chứa tất cả các quy tắn nghiệp vụ của ứng dụng. Ví dụ LoginUseCase kiểm tra email không được để trống, mật khẩu phải có ít nhất 6 ký tự, và ủy quyền cho Repository thực hiện đăng nhập. Việc tách logic nghiệp vụ ra khỏi ViewModel giúp code dễ test hơn và tái sử dụng được ở nhiều nơi.

### 4.3 DATA LAYER (Android)
Data Layer implement các interfaces định nghĩa trong Domain Layer và quản lý việc truy xuất dữ liệu từ các nguồn bên ngoài. Remote Data Sources sử dụng Retrofit để gọi REST API, Gson hoặc Moshi để parse JSON, và OkHttp với các interceptors để xử lý authentication và logging. Local Data Sources sử dụng Room Database để lưu trữ dữ liệu cục bộ, SharedPreferences hoặc EncryptedSharedPreferences để lưu trữ preferences và tokens bảo mật. Repository Implementations implement các Repository interfaces, điều phối giữa remote và local data sources, và xử lý caching. Mappers chuyển đổi giữa DTO (Data Transfer Object) và Domain Model.

### 4.4 BACKEND LAYER (Spring Boot)
Backend được tổ chức theo mô hình phân lớp truyền thống của Spring. Controller Layer tiếp nhận các HTTP requests, validate dữ liệu đầu vào với Jakarta Validation, gọi Service layer để xử lý nghiệp vụ, và trả về HTTP responses. Service Layer chứa logic nghiệp vụ chính, gọi Repository để truy cập dữ liệu, và xử lý các nghiệp vụ phức tạp. Repository Layer sử dụng Spring Data JPA để truy cập cơ sở dữ liệu, định nghĩa các phương thức truy vấn, và quản lý các transaction. Security Layer sử dụng Spring Security với custom filters, xác thực Firebase tokens, và quản lý quyền truy cập.

---

## 5. HƯỚNG DẪN PHÁT TRIỂN

### 5.1 Quy tắc đặt tên
Việc đặt tên nhất quán là rất quan trọng để duy trì code dễ đọc và dễ bảo trì. Đối với các Class, sử dụng PascalCase với ví dụ như LoginFragment, AuthViewModel, AuthRepository. Đối với các phương thức và biến, sử dụng camelCase với ví dụ như login(), getProfile(), authToken. Đối với các Package, sử dụng lowercase với ví dụ như com.vaultpool.customer, com.vaultpool.auth. Đối với các hằng số, sử dụng UPPER_SNAKE_CASE với ví dụ như BASE_URL, DEFAULT_TIMEOUT.

### 5.2 Quy trình thêm chức năng mới
Khi cần thêm một chức năng mới vào hệ thống, các thành viên nên tuân theo quy trình sau để đảm bảo tính nhất quán và chất lượng code.

Đầu tiên, tạo Domain Model trong layer domain nếu cần thiết. Model này nên là Plain Old Java Object (POJO) không phụ thuộc vào framework nào. Tiếp theo, định nghĩa Repository Interface trong domain/repository/. Interface này định nghĩa các phương thức mà data layer cần implement.

Sau đó, tạo UseCase trong domain/usecase/. UseCase nên chứa tất cả logic nghiệp vụ liên quan đến tính năng mới, có thể sử dụng các Repository interfaces thông qua dependency injection, và trả về Result<T> để xử lý cả success và failure cases.

Tiếp theo, implement Repository trong data/repository/. Implementation nên implement interface đã định nghĩa trong domain layer, có thể sử dụng Retrofit cho API calls hoặc Room cho local storage, và xử lý việc map giữa DTO và Domain model.

Sau đó, tạo ViewModel trong presentation/ui/. ViewModel nên chứa LiveData hoặc StateFlow cho UI state, có reference đến UseCases thông qua constructor, xử lý các events từ UI và gọi UseCases, và cập nhật UI state dựa trên kết quả từ UseCases.

Cuối cùng, tạo hoặc cập nhật Fragment/Activity trong presentation/ui/. Fragment nên observe ViewModel's LiveData/StateFlow, gọi ViewModel methods khi có user interactions, và xử lý UI state changes để hiển thị đúng giao diện.

### 5.3 Xử lý lỗi
Hệ thống sử dụng Result<T> pattern để xử lý kết quả từ các UseCases. Result<T> có hai trạng thái chính: Success chứa dữ liệu khi operation thành công, và Failure chứa Throwable khi operation thất bại. ViewModel xử lý hai trạng thái này bằng cách gọi phương thức fold() hoặc kiểm tra isSuccess()/isFailure(), sau đó cập nhật UI state tương ứng.

Backend sử dụng GlobalExceptionHandler để bắt và xử lý các exceptions một cách tập trung. Các exceptions cụ thể như AuthException, ResourceNotFoundException được định nghĩa trong package exception và xử lý riêng biệt để trả về thông báo lỗi phù hợp cho client.

---

## 6. CẤU HÌNH MÔI TRƯỜNG

### 6.1 Cấu hình Backend
Backend service sử dụng các file cấu hình trong src/main/resources/. File application.yml chứa các cấu hình chính bao gồm server port (mặc định 8081), cấu hình database (MySQL với URL, username, password), cấu hình JPA/Hibernate, cấu hình Redis cho cache, cấu hình Firebase credentials path, cấu hình JWT secret và expiration time, và cấu hình Swagger/OpenAPI.

Các biến môi trường quan trọng cần được thiết lập khi deploy bao gồm DB_PASSWORD cho mật khẩu database, REDIS_PASSWORD cho mật khẩu Redis, FIREBASE_CREDENTIALS_PATH cho đường dẫn đến Firebase service account JSON file, và JWT_SECRET cho secret key dùng ký JWT tokens.

### 6.2 Cấu hình Android
Trong Android app, các URL và thông số được định nghĩa trong AppModule của Hilt. BASE_URL được thiết lập là http://10.0.2.2:8081/ cho Android Emulator (địa chỉ localhost từ emulator) hoặc http://<YOUR_IP>:8081/ cho thiết bị thật. Khi build trên thiết bị thật, cần thay thế <YOUR_IP> bằng địa chỉ IP thực của máy chủ backend.

Firebase được cấu hình thông qua google-services.json file được đặt trong thư mục app/. File này chứa các thông tin Firebase project bao gồm API key, application ID, và các cấu hình Firebase services.

---

## 7. API ENDPOINTS

### 7.1 Auth Service API
Dịch vụ auth cung cấp các endpoints sau để quản lý xác thực người dùng.

Endpoint POST /api/auth/register cho phép đăng ký người dùng mới với Firebase ID token. Request body chứa firebaseIdToken (token từ Firebase), email, fullName, và phone (tùy chọn). Response trả về ApiResponse<UserResponse> với thông tin người dùng đã tạo. Endpoint này không yêu cầu authentication.

Endpoint GET /api/auth/me trả về thông tin profile của người dùng hiện tại. Request cần header Authorization: Bearer <firebase_id_token>. Response trả về ApiResponse<UserResponse> với thông tin người dùng. Endpoint này yêu cầu authentication.

### 7.2 Response Format
Tất cả responses từ API tuân theo format chuẩn. Response thành công có success = true, message = "Operation successful", và data chứa dữ liệu. Response lỗi có success = false, message chứa thông báo lỗi, và errors (tùy chọn) chứa chi tiết các lỗi validation.

---

## 8. QUẢN LÝ PHIÊN BẢN VÀ GIT

### 8.1 Git Branching Strategy
Hệ thống sử dụng Git Flow đơn giản hóa để quản lý các phiên bản và tính năng. Branch main chứa code đã sẵn sàng cho production và chỉ được merge sau khi đã review và test kỹ lưỡng. Branch develop là branch tích hợp chính cho các tính năng đang phát triển. Branch feature/* được tạo từ develop để phát triển các tính năng mới, ví dụ feature/authentication, feature/booking. Branch bugfix/* được tạo từ develop hoặc main để sửa các lỗi. Branch release/* được tạo từ develop khi chuẩn bị release phiên bản mới.

### 8.2 Quy tắc đặt tên Branch
Tên branch nên theo quy tắc: feature/<tên-tính-năng> cho các tính năng mới như feature/user-profile, feature/booking-system; bugfix/<mô-tả-lỗi> cho các sửa lỗi như bugfix/login-error; release/<phiên-bản> cho các phiên bản release như release/v1.0.0.

---

## 9. TESTING

### 9.1 Unit Testing
Unit tests nên được viết cho tất cả các UseCases và ViewModels. UseCase tests kiểm tra logic nghiệp vụ bao gồm validation, xử lý các edge cases, và đảm bảo UseCase gọi đúng repository methods. ViewModel tests kiểm tra việc ViewModel xử lý các events đúng cách, cập nhật UI state chính xác, và gọi UseCases với đúng parameters.

### 9.2 Integration Testing
Backend integration tests sử dụng @SpringBootTest và MockMvc để test các API endpoints. Tests nên cover các happy paths và error scenarios, sử dụng in-memory database cho testing, và verify responses status codes và bodies.

---

## 10. TÀI LIỆU THAM KHẢO

Để hiểu thêm về các công nghệ được sử dụng trong hệ thống, các thành viên có thể tham khảo các nguồn sau đây.

Về Android Development, tài liệu chính thức của Android Developers cung cấp hướng dẫn đầy đủ về Android Architecture Components, Jetpack, và Best Practices. Hilt Documentation hướng dẫn chi tiết về Dependency Injection trong Android. Firebase Documentation cung cấp thông tin về Firebase Authentication và các Firebase services khác.

Về Spring Boot, Spring Boot Documentation là tài liệu tham khảo chính thức cho backend. Spring Security Reference cung cấp hướng dẫn về bảo mật trong Spring. Spring Data JPA Guide hướng dẫn về việc truy cập dữ liệu với JPA.

Về Architecture, Clean Architecture Blog của Uncle Bob giải thích lý thuyết đằng sau mô hình Clean Architecture. Android Architecture Samples cung cấp các ví dụ thực tế về cách áp dụng architecture patterns trong Android.

---

## 11. LIÊN HỆ VÀ HỖ TRỢ

Khi gặp vấn đề hoặc có câu hỏi về kiến trúc này, các thành viên nên tuân theo các bước sau. Đầu tiên, xem lại tài liệu này để tìm câu trả lời. Tiếp theo, kiểm tra code mẫu trong các module để hiểu cách implement. Sau đó, thảo luận trong channel của đội nhóm trên Slack hoặc Discord. Cuối cùng, liên hệ Tech Lead nếu cần hỗ trợ thêm.

---

**Phiên bản tài liệu:** 1.0
**Ngày tạo:** 2024
**Người tạo:** VAULTPOOL Team
