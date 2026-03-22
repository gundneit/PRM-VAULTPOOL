# ZaloPay Android SDK (ZPDK)

File **`zpdk-release-v3.1.aar`** không được commit lên Git (bản quyền / tải từ ZaloPay).

## Cách lấy file

1. Vào cổng **ZaloPay Developer** → mục **Downloads / SDK** (ví dụ: [developers.zalopay.vn — Downloads](https://developers.zalopay.vn/v1/downloads/) hoặc tài liệu lab của bạn).
2. Tải **Android ZPDK** (tên gần giống `zpdk-release-v3.1.aar`).
3. Đặt file vào **đúng thư mục này**:
   ```
   apps/android-customer/libs/zpdk-release-v3.1.aar
   ```
4. Trong Android Studio: **File → Sync Project with Gradle Files**, rồi **Build → Rebuild Project**.

Dependency được khai báo qua **`flatDir`** trong `settings.gradle` + `implementation(name: "zpdk-release-v3.1", ext: "aar")` trong `build.gradle` để AAR luôn vào compile classpath.

**Import đúng với ZPDK v3.1:** `vn.zalopay.sdk.listeners.PayOrderListener` (không phải `vn.zalopay.listeners`).

**Chữ ký `PayOrderListener` (v3.1):** `onPaymentSucceeded(String,String,String)`, `onPaymentCanceled(String,String)`, `onPaymentError(ZaloPayError,String,String)`.
