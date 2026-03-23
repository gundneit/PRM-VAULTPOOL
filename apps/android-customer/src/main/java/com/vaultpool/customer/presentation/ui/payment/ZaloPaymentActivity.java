package com.vaultpool.customer.presentation.ui.payment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;
import com.vaultpool.customer.ServiceLocator;
import com.vaultpool.customer.data.local.prefs.PreferencesManager;
import com.vaultpool.customer.data.remote.api.BookingApi;
import com.vaultpool.customer.data.remote.dto.ApiResponse;
import com.vaultpool.customer.data.remote.dto.BookingResponseDto;
import com.vaultpool.customer.databinding.ActivityZaloPaymentBinding;
import com.vaultpool.customer.presentation.ui.main.MainActivity;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import vn.zalopay.sdk.listeners.PayOrderListener;
import vn.zalopay.sdk.Environment;
import vn.zalopay.sdk.ZaloPaySDK;
import vn.zalopay.sdk.ZaloPayError;

public class ZaloPaymentActivity extends AppCompatActivity {

    public static final String EXTRA_ZP_TRANS_TOKEN = "extra_zp_trans_token";
    public static final String EXTRA_BOOKING_ID = "extra_booking_id";
    public static final String EXTRA_PAID_BOOKING_ID = "paid_booking_id";
    private static final String SCHEME_ZALOPAY = "demozpdk";

    private static final int ZALOPAY_APP_ID_SANDBOX = 2554;
    private static final String URI_SCHEME = "demozpdk://app";
    private static final String PREFS_PAYMENT_SESSION = "zalo_payment_session";
    private static final String KEY_BOOKING_ID = "booking_id";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_TOKEN_LAUNCHED_AT = "token_launched_at";
    private static final String KEY_SESSION_CREATED_AT = "session_created_at";
    private static final long TOKEN_LAUNCH_DEDUP_MS = 120000L;
    private static final long SESSION_TTL_MS = 15 * 60 * 1000L;
    private static final long CALLBACK_FALLBACK_DELAY_MS = 400L;

    private ActivityZaloPaymentBinding binding;
    private boolean hasNavigated = false;
    private boolean hasStartedPayment = false;
    private String currentZpToken;
    private long bookingId = -1L;
    private BookingApi bookingApi;
    private PreferencesManager preferencesManager;
    private final CompositeDisposable disposables = new CompositeDisposable();
    private final Handler callbackHandler = new Handler(Looper.getMainLooper());
    private final Runnable callbackFallbackRunnable = this::resolveBookingOutcomeImmediately;

    private enum BookingResolution {
        SUCCESS,
        FAILURE,
        PENDING,
        NOT_FOUND
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityZaloPaymentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        bookingApi = ServiceLocator.getInstance().getBookingApi();
        preferencesManager = ServiceLocator.getInstance().getPreferencesManager();

        // Init SDK trước mọi nhánh xử lý callback/deeplink
        try {
            ZaloPaySDK.init(ZALOPAY_APP_ID_SANDBOX, Environment.SANDBOX);
            handleIncomingIntent(getIntent());
        } catch (Throwable ignored) {
            navigateToBookings("PENDING_PAYMENT");
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        try {
            handleIncomingIntent(intent);
        } catch (Throwable ignored) {
            navigateToBookings("PENDING_PAYMENT");
        }
    }

    private void handleIncomingIntent(Intent intent) {
        if (intent == null || hasNavigated) {
            return;
        }

        restoreOrAssignBookingId(intent);

        // Ưu tiên xử lý callback deep-link từ ZaloPay trước,
        // tránh bị nhánh token session nuốt mất onResult().
        if (isZaloDeepLinkIntent(intent)) {
            handleZaloCallbackIntent(intent);
            return;
        }

        String zpTransToken = intent.getStringExtra(EXTRA_ZP_TRANS_TOKEN);
        if (!hasPaymentToken(zpTransToken)) {
            zpTransToken = restoreTokenFromSession();
        }
        if (hasPaymentToken(zpTransToken)) {
            handlePaymentTokenIntent(zpTransToken);
            return;
        }

        if (bookingId > 0) {
            resolveBookingOutcomeImmediately();
            return;
        }

        // Không có dữ liệu hợp lệ để tiếp tục flow thì thoát an toàn về pending.
        navigateToBookings("PENDING_PAYMENT");
    }

    private void restoreOrAssignBookingId(Intent intent) {
        long incomingBookingId = intent.getLongExtra(EXTRA_BOOKING_ID, -1L);
        if (incomingBookingId > 0) {
            bookingId = incomingBookingId;
            persistSession(bookingId, currentZpToken);
        } else if (bookingId <= 0) {
            bookingId = restoreBookingIdFromSession();
        }
    }

    private boolean hasPaymentToken(String token) {
        return token != null && !token.trim().isEmpty();
    }

    private void handlePaymentTokenIntent(String zpTransToken) {
        // Guard chong start payOrder lap neu activity bi receive lai cung token.
        if (hasStartedPayment && zpTransToken.equals(currentZpToken)) {
            if (isTokenRecentlyLaunched(zpTransToken)) {
                resolveBookingOutcomeImmediately();
            } else {
                updateStatusText("Đang mở ZaloPay...");
                markTokenLaunched(zpTransToken);
                startZaloPay(zpTransToken);
            }
            return;
        }

        hasStartedPayment = true;
        currentZpToken = zpTransToken;
        persistSession(bookingId, currentZpToken);

        if (isTokenRecentlyLaunched(zpTransToken)) {
            resolveBookingOutcomeImmediately();
            return;
        }

        updateStatusText("Đang mở ZaloPay...");
        markTokenLaunched(zpTransToken);
        startZaloPay(zpTransToken);
    }

    private void handleZaloCallbackIntent(Intent intent) {
        updateStatusText("Đang nhận kết quả...");
        if (looksLikeCancelOrErrorDeepLink(intent)) {
            navigateToBookings("PENDING_PAYMENT");
            return;
        }
        // Không gọi onResult khi app quay về bằng deep-link để tránh crash từ SDK ở một
        // số thiết bị.
        // Luôn resolve qua backend để quyết định điều hướng.
        callbackHandler.removeCallbacks(callbackFallbackRunnable);
        callbackHandler.post(callbackFallbackRunnable);
    }

    private boolean looksLikeCancelOrErrorDeepLink(Intent intent) {
        if (intent == null || intent.getData() == null) {
            return false;
        }
        String value = intent.getData().toString();
        if (value == null) {
            return false;
        }
        String normalized = value.toLowerCase(Locale.ROOT);
        return normalized.contains("cancel")
                || normalized.contains("failed")
                || normalized.contains("error");
    }

    private boolean isZaloDeepLinkIntent(Intent intent) {
        if (intent == null) {
            return false;
        }
        if (!Intent.ACTION_VIEW.equals(intent.getAction())) {
            return false;
        }
        if (intent.getData() == null) {
            return false;
        }
        String scheme = intent.getData().getScheme();
        return SCHEME_ZALOPAY.equalsIgnoreCase(scheme);
    }

    private void startZaloPay(String zpTransToken) {
        try {
            ZaloPaySDK.getInstance().payOrder(
                    this,
                    zpTransToken,
                    URI_SCHEME,
                    new PayOrderListener() {
                        @Override
                        public void onPaymentSucceeded(String transactionId, String transToken, String appTransId) {
                            runOnUiThread(() -> {
                                updateStatusText("Thanh toán thành công...");
                                navigateToBookings("CONFIRMED");
                            });
                        }

                        @Override
                        public void onPaymentCanceled(String zpTransTokenParam, String message) {
                            runOnUiThread(() -> {
                                updateStatusText("Đã hủy thanh toán.");
                                navigateToBookings("PENDING_PAYMENT");
                            });
                        }

                        @Override
                        public void onPaymentError(ZaloPayError zaloPayError, String zpTransTokenParam,
                                String message) {
                            runOnUiThread(() -> navigateToBookings("PENDING_PAYMENT"));
                        }
                    });
        } catch (Throwable ignored) {
            navigateToBookings("PENDING_PAYMENT");
        }
    }

    private void navigateToBookings(String tabStatus) {
        if (hasNavigated)
            return;
        hasNavigated = true;
        stopPendingOperations();
        doNavigate(tabStatus);
    }

    private void resolveBookingOutcomeImmediately() {
        if (hasNavigated || bookingId <= 0 || bookingApi == null || preferencesManager == null) {
            navigateToBookings("PENDING_PAYMENT");
            return;
        }

        String token = preferencesManager != null ? preferencesManager.getFirebaseToken() : null;
        if (token == null || token.trim().isEmpty()) {
            navigateToBookings("PENDING_PAYMENT");
            return;
        }

        String bearer = token.startsWith("Bearer ") ? token : "Bearer " + token;
        disposables.add(
                bookingApi.getMyBookings(bearer, "CONFIRMED")
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(response -> {
                            if (hasNavigated) {
                                return;
                            }
                            if (containsBooking(response != null ? response.getData() : null)) {
                                updateStatusText("Thanh toán thành công...");
                                navigateToBookings("CONFIRMED");
                                return;
                            }
                            resolveBookingOutcomeFallback(bearer);
                        }, throwable -> resolveBookingOutcomeFallback(bearer)));
    }

    private void resolveBookingOutcomeFallback(String bearer) {
        if (hasNavigated) {
            return;
        }
        disposables.add(
                bookingApi.getMyBookings(bearer, null)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(this::handleBookingPollResponse,
                                throwable -> navigateToBookings("PENDING_PAYMENT")));
    }

    private void handleBookingPollResponse(ApiResponse<List<BookingResponseDto>> response) {
        if (hasNavigated) {
            return;
        }

        BookingResolution resolution = resolveBookingState(response != null ? response.getData() : null);
        if (resolution == BookingResolution.SUCCESS) {
            updateStatusText("Thanh toán thành công...");
            navigateToBookings("CONFIRMED");
            return;
        }
        if (resolution == BookingResolution.FAILURE) {
            navigateToBookings("PENDING_PAYMENT");
            return;
        }
        navigateToBookings("PENDING_PAYMENT");
    }

    private BookingResolution resolveBookingState(List<BookingResponseDto> bookings) {
        BookingResponseDto booking = findBookingById(bookings);
        if (booking == null) {
            return BookingResolution.NOT_FOUND;
        }

        String bookingStatus = normalizeStatus(booking.getStatus());
        String paymentStatus = normalizeStatus(booking.getPaymentStatus());

        if (isSuccessStatus(bookingStatus, paymentStatus)) {
            return BookingResolution.SUCCESS;
        }
        if (isFailureStatus(bookingStatus, paymentStatus)) {
            return BookingResolution.FAILURE;
        }
        return BookingResolution.PENDING;
    }

    private BookingResponseDto findBookingById(List<BookingResponseDto> bookings) {
        if (bookings == null || bookings.isEmpty() || bookingId <= 0) {
            return null;
        }
        for (BookingResponseDto item : bookings) {
            if (item != null && item.getId() != null && Objects.equals(item.getId(), bookingId)) {
                return item;
            }
        }
        return null;
    }

    private boolean containsBooking(List<BookingResponseDto> bookings) {
        return findBookingById(bookings) != null;
    }

    private String normalizeStatus(String value) {
        return value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
    }

    private boolean isSuccessStatus(String bookingStatus, String paymentStatus) {
        return "CONFIRMED".equals(bookingStatus) || "SUCCESS".equals(paymentStatus);
    }

    private boolean isFailureStatus(String bookingStatus, String paymentStatus) {
        return "CANCELED".equals(bookingStatus)
                || "CANCELLED".equals(bookingStatus)
                || "FAILED".equals(bookingStatus)
                || "EXPIRED".equals(bookingStatus)
                || "CANCELED".equals(paymentStatus)
                || "CANCELLED".equals(paymentStatus)
                || "FAILED".equals(paymentStatus)
                || "EXPIRED".equals(paymentStatus);
    }

    private void doNavigate(String tabStatus) {
        // Dùng FLAG_ACTIVITY_CLEAR_TOP thay vì CLEAR_TASK
        // để không tạo task mới, avoid singleTask conflict
        Intent intent = new Intent(ZaloPaymentActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra("navigate_to", "bookings");
        intent.putExtra("tab_status", tabStatus);
        if (bookingId > 0) {
            intent.putExtra(EXTRA_PAID_BOOKING_ID, bookingId);
        }
        clearSession();
        try {
            startActivity(intent);
            finish();
        } catch (Throwable ignored) {
            // Fail-safe: vẫn mở lại MainActivity để tránh bị văng khỏi app.
            Intent fallback = new Intent(ZaloPaymentActivity.this, MainActivity.class);
            fallback.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            fallback.putExtra("navigate_to", "bookings");
            fallback.putExtra("tab_status", "PENDING_PAYMENT");
            startActivity(fallback);
            finish();
        }
    }

    private SharedPreferences getSessionPrefs() {
        return getSharedPreferences(PREFS_PAYMENT_SESSION, MODE_PRIVATE);
    }

    private void persistSession(long bookingIdValue, String tokenValue) {
        SharedPreferences.Editor editor = getSessionPrefs().edit();
        if (bookingIdValue > 0) {
            editor.putLong(KEY_BOOKING_ID, bookingIdValue);
        }
        if (tokenValue != null && !tokenValue.trim().isEmpty()) {
            editor.putString(KEY_TOKEN, tokenValue);
        }
        editor.putLong(KEY_SESSION_CREATED_AT, System.currentTimeMillis());
        editor.apply();
    }

    private long restoreBookingIdFromSession() {
        long createdAt = getSessionPrefs().getLong(KEY_SESSION_CREATED_AT, 0L);
        if (createdAt <= 0 || System.currentTimeMillis() - createdAt > SESSION_TTL_MS) {
            clearSession();
            return -1L;
        }
        return getSessionPrefs().getLong(KEY_BOOKING_ID, -1L);
    }

    private String restoreTokenFromSession() {
        long createdAt = getSessionPrefs().getLong(KEY_SESSION_CREATED_AT, 0L);
        if (createdAt <= 0 || System.currentTimeMillis() - createdAt > SESSION_TTL_MS) {
            clearSession();
            return null;
        }
        return getSessionPrefs().getString(KEY_TOKEN, null);
    }

    private boolean isTokenRecentlyLaunched(String token) {
        SharedPreferences prefs = getSessionPrefs();
        String lastToken = prefs.getString(KEY_TOKEN, null);
        if (lastToken == null || !lastToken.equals(token)) {
            return false;
        }
        long launchedAt = prefs.getLong(KEY_TOKEN_LAUNCHED_AT, 0L);
        return launchedAt > 0 && (System.currentTimeMillis() - launchedAt) < TOKEN_LAUNCH_DEDUP_MS;
    }

    private void markTokenLaunched(String token) {
        getSessionPrefs().edit()
                .putString(KEY_TOKEN, token)
                .putLong(KEY_TOKEN_LAUNCHED_AT, System.currentTimeMillis())
                .apply();
    }

    private void clearSession() {
        stopPendingOperations();
        getSessionPrefs().edit().clear().apply();
    }

    private void stopPendingOperations() {
        callbackHandler.removeCallbacks(callbackFallbackRunnable);
        disposables.clear();
    }

    private void updateStatusText(String text) {
        if (binding != null) {
            binding.tvStatus.setText(text);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        callbackHandler.removeCallbacks(callbackFallbackRunnable);
    }

    @Override
    protected void onDestroy() {
        stopPendingOperations();
        super.onDestroy();
        binding = null;
    }
}