# Hướng dẫn test Backend (auth-service)

## File chạy (entry point)

- **Backend:** `src/main/java/com/lavela/pool/LaVelaApplication.java` — class có `main()` để khởi động Spring Boot.

## 1. Chạy backend

```powershell
cd backends\auth-service

# Dùng MySQL local (không cần TiDB)
mvn spring-boot:run -Dspring.profiles.active=dev,local-mysql

# Hoặc dùng TiDB Cloud (cần set TIDB_USERNAME + TIDB_PASSWORD với username có prefix)
mvn spring-boot:run
```

Backend chạy tại: **http://localhost:8080**

### Chạy trong VS Code / Cursor

**Cách 1 — Mở cả repo (PRM-VAULTPOOL):**

1. Cài **Extension Pack for Java** (Microsoft).
2. Đợi Java Language Server load xong (góc dưới phải: "Loading projects..." biến mất).
3. **Run and Debug** (Ctrl+Shift+D) → chọn **"Chạy Backend (auth-service)"** → bấm nút play (F5).  
   *Không* bấm nút Run trên file `LaVelaApplication.java` khi đang mở cả repo — dễ bị báo "no main method" vì VS Code đang dùng project Gradle (Android) thay vì Maven (backend).

**Cách 2 — Chỉ mở folder backend (tránh lỗi "no main method"):**

1. File → Open Folder → chọn **`backends/auth-service`** (chỉ thư mục này).
2. Đợi Maven load xong.
3. Mở `LaVelaApplication.java` → bấm **Run** hoặc **F5**.

**Nếu vẫn báo "no main method":**

- Command Palette (Ctrl+Shift+P) → gõ **Java: Clean Java Language Server Workspace** → Reload and delete.
- Hoặc **Java: Reload Projects** để load lại project Maven.
- Sau đó chạy bằng **Run and Debug** → **"Chạy Backend (auth-service)"**.

---

## 2. Swagger UI (giao diện test API)

Mở trình duyệt:

- **http://localhost:8080/swagger-ui.html**

Tại đây có thể:
- Xem danh sách API
- Gọi thử từng endpoint (nhập body, bấm Execute)
- Các API cần đăng nhập: bấm **Authorize** → nhập `Bearer <token>`

---

## 3. Test nhanh bằng curl / Postman

### API không cần token (public)

```bash
# Danh sách hồ bơi
curl http://localhost:8080/pools

# Chi tiết 1 hồ
curl http://localhost:8080/pools/1

# Slot theo ngày (đổi pool id và ngày)
curl "http://localhost:8080/pools/1/slots?date=2026-03-20"
```

### Đăng ký (tạo user + lấy thông tin)

```bash
# Đăng ký (không cần token)
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d "{\"firebaseUid\":\"test-uid-001\",\"email\":\"test@example.com\",\"fullName\":\"Test User\",\"phone\":\"0901234567\"}"
```

### API cần token (Customer / Staff / Admin)

Lấy Firebase ID token từ app Android hoặc Firebase Console, rồi gọi:

```bash
# Lấy thông tin user đang đăng nhập
curl http://localhost:8080/api/auth/me -H "Authorization: Bearer <FIREBASE_ID_TOKEN>"

# Tạo booking (cần token user đã verify)
curl -X POST http://localhost:8080/bookings \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d "{\"slotId\":1,\"qty\":2}"

# Danh sách booking của tôi
curl "http://localhost:8080/bookings?me=true" -H "Authorization: Bearer <TOKEN>"

# Tạo payment bằng ZaloPay (BE2)
curl -X POST http://localhost:8080/payments \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d "{\"bookingId\":1,\"method\":\"ZALOPAY\"}"
```

### Webhook ZaloPay (server-to-server) — không cần token

ZaloPay callback endpoint:
`POST /webhooks/payment/ZALOPAY`

Body callback theo spec:
{
  "data": "{\"app_trans_id\":\"240321_BDF4B21D6430\"}", #đổi
  "mac": "87b160fde175c9af90c36e056dd23a4b7c4e224b6290e0405d2e4bb309404e92", đổi
  "type": 1
}

**Lưu ý Swagger / test tay**

- `mac` phải là **hex chữ thường**, HMAC-SHA256 với đúng **key2 mà backend đang chạy** (trùng `ZALOPAY_SANDBOX_KEY2` / `application-dev.yml`). MAC tính trên **đúng chuỗi** trong field `data` (UTF-8), ví dụ nội dung logic: `{"app_trans_id":"YYMMDD_XXXXX"}` — không thêm space, không đổi thứ tự key.
- Với **key2 sandbox mặc định** trong `application-dev.yml`, chuỗi `{"app_trans_id":"240321_BDF4B21D6430"}` có MAC đúng là `87b160fde175c9af90c36e056dd23a4b7c4e224b6290e0405d2e4bb309404e92`. Nếu MAC khác → backend báo mismatch.
- **Không bắt buộc** gửi header `Authorization` cho webhook; có cũng không sao.
- Endpoint webhook luôn trả JSON kiểu ZaloPay: `{ "return_code": 1|2, "return_message": "..." }` (không dùng wrapper `ApiResponse`).
- Nếu Swagger báo lỗi parse JSON kiểu *Unexpected character '{' … expecting double-quote*: thường do body **sai cú pháp** (dư `{`, thiếu dấu phẩy, key không có `"`, hoặc paste nhầm). Chuẩn: mỗi key phải có `"`. Có thể dùng **`data` là object** thay vì chuỗi escape, ví dụ:
  `{"data":{"app_trans_id":"YYMMDD_XXX"},"mac":"<hex>","type":1}`  
  (khi đó MAC phải tính trên **chuỗi JSON tương ứng** sau khi serialize, thường giống `{"app_trans_id":"YYMMDD_XXX"}` không có space thừa.)

**Windows PowerShell, VS CODE TERMINAL — tính MAC**

```powershell
  $key2 = "trMrHtvjo6myautxDUiAcYsVtaeQ8nhf"                               
 $data  = '{"app_trans_id":"260321_885B00595BB4"}'   # xem providerTxnId                           
 $h = [System.Security.Cryptography.HMACSHA256]::new([Text.Encoding]::UTF8.GetBytes($key2))
 -join ($h.ComputeHash([Text.Encoding]::UTF8.GetBytes($data)) | ForEach-Object { $_.ToString("x2") })
```

Ví dụ (giả lập THÀNH CÔNG):

```bash
# Thay APP_TRANS_ID bằng providerTxnId bạn vừa tạo
APP_TRANS_ID="MOCK_APP_TRANS_ID"

# data là một JSON string (chuỗi) chứa app_trans_id
DATA="{\"app_trans_id\":\"$APP_TRANS_ID\"}"

# Tạo MAC hex (HMACSHA256 với key2). Bạn cần thay ZALOPAY_KEY2 bằng key2 sandbox của mình.
MAC=$(node -e "const crypto=require('crypto'); const key=process.env.ZALOPAY_KEY2; const data=process.env.DATA; console.log(crypto.createHmac('sha256', key).update(data).digest('hex'));")

curl -X POST http://localhost:8080/webhooks/payment/ZALOPAY \
  -H "Content-Type: application/json" \
  -d "{\"data\":\"$DATA\",\"mac\":\"$MAC\",\"type\":1}" \
  -e ZALOPAY_KEY2="${ZALOPAY_KEY2}" -e DATA="${DATA}"
```

Ví dụ (giả lập THẤT BẠI/CANCEL):

Lưu ý: backend của bạn sẽ **không** quyết định success/fail chỉ dựa trên webhook payload.
Sau khi verify MAC xong, backend sẽ tự gọi **ZaloPay order-query** (`POST /v2/query`) để lấy `return_code`.
Vì vậy để test nhánh FAILED/CANCELED:
- Bạn phải tạo payment trên ZaloPay và thực sự **cancel/failed** trên app (sandbox).
- Sau đó dùng **cùng** `app_trans_id` (providerTxnId) để gọi lại webhook này.

```bash
# Thay APP_TRANS_ID bằng providerTxnId bạn vừa tạo
APP_TRANS_ID="MOCK_APP_TRANS_ID"
DATA="{\"app_trans_id\":\"$APP_TRANS_ID\"}"
MAC=$(node -e "const crypto=require('crypto'); const key=process.env.ZALOPAY_KEY2; const data=process.env.DATA; console.log(crypto.createHmac('sha256', key).update(data).digest('hex'));")

curl -X POST http://localhost:8080/webhooks/payment/ZALOPAY \
  -H "Content-Type: application/json" \
  -d "{\"data\":\"$DATA\",\"mac\":\"$MAC\",\"type\":1}" \
  -e ZALOPAY_KEY2="${ZALOPAY_KEY2}" -e DATA="${DATA}"
```

Kỳ vọng kết quả backend:
- Payment `.status` -> `FAILED`
- Booking `.status` -> `FAILED` (theo code hiện tại)

Lưu ý: khi nhận callback, backend sẽ tự động gọi **ZaloPay order-query** (`/v2/query`) để lấy `return_code` nhằm quyết định `SUCCESS` hay `FAILED`. Vì vậy test cần cấu hình sandbox đúng và có kết nối tới ZaloPay.

`providerTxnId` lấy từ response của `POST /payments` (field `providerTxnId`).

### Staff API (token user có role STAFF hoặc ADMIN)

```bash
# Danh sách booking theo ngày/slot
curl "http://localhost:8080/api/staff/bookings?date=2026-03-20" -H "Authorization: Bearer <STAFF_TOKEN>"
curl "http://localhost:8080/api/staff/bookings?slotId=1" -H "Authorization: Bearer <STAFF_TOKEN>"

# Check-in (staff scan QR)
curl -X POST http://localhost:8080/bookings/1/checkin \
  -H "Authorization: Bearer <STAFF_TOKEN>" \
  -H "Content-Type: application/json" \
  -d "{\"hash\":\"VP-XXXXXXXX\"}"

# Hoàn tiền (BE2)
curl -X POST http://localhost:8080/api/staff/payments/1/refund \
  -H "Authorization: Bearer <STAFF_TOKEN>"
```

---

## 4. Luồng test đầy đủ (Booking + Payment)

1. **GET /pools** → lấy `poolId`
2. **GET /pools/{id}/slots?date=YYYY-MM-DD** → lấy `slotId`
3. **POST /bookings** (body: `slotId`, `qty`) với token → nhận `booking.id`, `expiresAt`
4. **POST /payments** (body: `bookingId`, `method`) với token → nhận `redirectUrl`, `providerTxnId`
5. **POST /webhooks/payment/ZALOPAY** (body: `{data, mac, type: 1}`) không token → booking chuyển CONFIRMED
6. **GET /bookings/{id}** với token → kiểm tra `status: CONFIRMED` (nhánh thành công)

---

## 4b. Luồng test thất bại (Booking + Payment)

1. Thực hiện bước **1-4** như trên để tạo `booking` và `payment` trên ZaloPay.
2. Mở ZaloPay và thực hiện thao tác **cancel/failed** cho đơn thanh toán trên app (sandbox).
3. Sau đó gọi:
   - `POST /webhooks/payment/ZALOPAY` với cùng `app_trans_id` (tức `providerTxnId`) và `mac` đúng.
4. `GET /bookings/{id}` với token → kiểm tra `status: FAILED`

---

## 5. Dev token (nếu bật dev-auth)

Trong `application-dev.yml` có thể bật `app.dev-auth` với `bearer-token: dev-admin-token`. Khi đó có thể dùng:

```bash
curl http://localhost:8080/api/auth/me -H "Authorization: Bearer dev-admin-token"
```

để test mà không cần Firebase (tùy cách BE1 đã cấu hình dev-auth).

---

## 6. Build app Android trên Android Studio — ZaloPay ZPDK

Module `android-customer` dùng `flatDir` + `implementation(name: "zpdk-release-v3.1", ext: "aar")` (xem `settings.gradle`).

Nếu **chưa có file `.aar`** trong `apps/android-customer/libs/`, build sẽ không tìm thấy class ZaloPay.

**Import đúng với `zpdk-release-v3.1.aar`:** `vn.zalopay.sdk.listeners.PayOrderListener` (package `vn.zalopay.listeners` **không** tồn tại trong AAR này).

**Cách xử lý khi thiếu SDK:**

1. Tải **ZaloPay ZPDK Android** (`zpdk-release-v3.1.aar`) từ **ZaloPay Developer / Downloads** hoặc tài liệu lab.
2. Copy vào: `apps/android-customer/libs/zpdk-release-v3.1.aar`
3. **File → Sync Project with Gradle Files** → **Build → Rebuild Project**.

Chi tiết: `apps/android-customer/libs/README.md`.

---

## 7. Chạy app Android trên **máy thật** (điện thoại)

### A. Trên PC — backend phải chạy và điện thoại gọi được

1. **PC và điện thoại cùng mạng Wi‑Fi** (hoặc USB tethering từ điện thoại → PC vẫn có IP LAN; quan trọng là điện thoại ping được IP PC).
2. **Xem IPv4 của PC:** PowerShell → `ipconfig` → dòng **IPv4 Address** của card Wi‑Fi/Ethernet (ví dụ `192.168.1.105`).
3. **File `local.properties` ở thư mục gốc repo** (cùng cấp `settings.gradle`), thêm hoặc sửa:
   ```properties
   BACKEND_BASE_URL=http://<IPv4_PC>:8080/
   ```
   (có dấu **`/`** ở cuối). Emulator không cần dòng này — mặc định app dùng `http://10.0.2.2:8080/`.
4. **Chạy backend** (VS Code / Cursor / terminal):
   ```powershell
   cd backends\auth-service
   mvn spring-boot:run "-Dspring-boot.run.profiles=dev,local-mysql"
   ```
   (PowerShell: nhớ **ngoặc kép** quanh `-D...`). Backend lắng nghe **port 8080**.
5. **Windows Firewall:** nếu app báo không kết nối được API, cho phép **Java** hoặc **port 8080** inbound (Private network).

### B. Android Studio

1. Trên điện thoại: **Cài đặt → Giới thiệu về điện thoại** → bấm **Số bản dựng** 7 lần → bật **Tùy chọn nhà phát triển** → bật **Gỡ lỗi USB**.
2. Cắm USB, chọn chế độ **File transfer (MTP)** nếu được hỏi.
3. Android Studio: trên thanh công cụ chọn **thiết bị** = điện thoại của bạn (không phải emulator).
4. **File → Sync Project with Gradle Files** (sau khi sửa `local.properties`).
5. Bấm **Run ▶** (module `android-customer`).

### C. VS Code / Cursor (chỉ backend + chỉnh file)

- VS Code **không bắt buộc** để build APK: dùng để sửa code, chạy backend (terminal hoặc **Run and Debug** như mục §1), và chỉnh `local.properties`.
- Build/cài lên máy thật: nên dùng **Android Studio** (hoặc cài Gradle + `adb` rồi tự chạy lệnh build — phức tạp hơn).

### D. Kiểm tra nhanh

- Trên điện thoại, mở trình duyệt: `http://<IPv4_PC>:8080/swagger-ui.html` — nếu không mở được thì app cũng không gọi được API (sai IP, firewall, khác Wi‑Fi).

### E. Điện thoại **không mở được** trang (timeout / không kết nối được)

Làm lần lượt:

1. **Trên PC**, mở `http://localhost:8080/swagger-ui.html` — phải được. Nếu không → backend chưa chạy hoặc sai port.
2. **Cùng Wi‑Fi:** tắt **dữ liệu di động** trên điện thoại khi thử (tránh DNS/route lệch). PC không dùng VPN chặn LAN (thử tắt VPN).
3. **Đúng IPv4:** `ipconfig` → lấy IP của **Wi‑Fi** (Wireless LAN adapter), **không** lấy `169.254.x.x`, tránh nhầm **vEthernet / WSL / VMware** (chỉ dùng IP card đang ra internet).
4. **URL trên điện thoại:** `http://192.168.x.x:8080/swagger-ui.html` — dùng **`http`**, không gõ `https`; không gõ `localhost` trên điện thoại.
5. **Firewall:** rule **Inbound TCP 8080**, profile **Private**; Wi‑Fi Windows đặt là **Private** (Settings → Network → Wi‑Fi → properties).
6. **Router / Wi‑Fi khách (Guest):** một số mạng **cô lập thiết bị** (AP isolation) — điện thoại không ping/truy cập được PC. Thử Wi‑Fi chính (không phải guest).
7. **Thử trên PC** mở `http://<IPv4 đó>:8080/swagger-ui.html` (chính IP LAN của máy) — nếu PC cũng không mở được bằng IP LAN thì xem lại firewall / binding.

Backend đã cấu hình `server.address: 0.0.0.0` trong `application.yml` để lắng nghe trên mọi giao diện (phục vụ LAN).
