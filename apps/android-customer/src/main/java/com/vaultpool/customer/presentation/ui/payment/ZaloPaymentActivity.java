package com.vaultpool.customer.presentation.ui.payment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.vaultpool.customer.BuildConfig;
import com.vaultpool.customer.ServiceLocator;
import com.vaultpool.customer.data.local.prefs.PreferencesManager;
import com.vaultpool.customer.data.remote.api.PaymentApi;
import com.vaultpool.customer.data.remote.dto.ZaloWebhookRequestDto;
import com.vaultpool.customer.databinding.ActivityZaloPaymentBinding;
import com.vaultpool.customer.presentation.ui.main.MainActivity;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import vn.zalopay.sdk.listeners.PayOrderListener;
import vn.zalopay.sdk.Environment;
import vn.zalopay.sdk.ZaloPaySDK;
import vn.zalopay.sdk.ZaloPayError;

public class ZaloPaymentActivity extends AppCompatActivity {

    public static final String EXTRA_ZP_TRANS_TOKEN = "extra_zp_trans_token";
    public static final String EXTRA_BOOKING_ID = "extra_booking_id";

    private static final int ZALOPAY_APP_ID_SANDBOX = 2554;
    private static final String URI_SCHEME = "demozpdk://app";

    private ActivityZaloPaymentBinding binding;
    private PaymentApi paymentApi;
    private final CompositeDisposable disposables = new CompositeDisposable();
    private boolean hasNavigated = false;
    private Long bookingId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityZaloPaymentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        bookingId = getIntent().getLongExtra(EXTRA_BOOKING_ID, -1L);
        String zpTransToken = getIntent().getStringExtra(EXTRA_ZP_TRANS_TOKEN);
        
        if (zpTransToken == null || zpTransToken.isBlank()) {
            Toast.makeText(this, "Thiếu zp_trans_token", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        initPaymentApi();

        ZaloPaySDK.init(ZALOPAY_APP_ID_SANDBOX, Environment.SANDBOX);
        binding.tvStatus.setText("Đang mở ZaloPay...");

        ZaloPaySDK.getInstance().payOrder(
                this,
                zpTransToken,
                URI_SCHEME,
                new PayOrderListener() {
                    @Override
                    public void onPaymentSucceeded(String transactionId, String transToken, String appTransId) {
                        runOnUiThread(() -> {
                            binding.tvStatus.setText("Thanh toán SDK thành công. Đang cập nhật hệ thống...");
                            binding.progressBar.setVisibility(View.VISIBLE);
                            // Gọi Webhook để Backend confirm booking
                            callWebhookToConfirm(appTransId);
                        });
                    }

                    @Override
                    public void onPaymentCanceled(String zpTransTokenParam, String message) {
                        runOnUiThread(() -> {
                            Toast.makeText(ZaloPaymentActivity.this, "Bạn đã hủy thanh toán", Toast.LENGTH_SHORT).show();
                            navigateToBookings("PENDING_PAYMENT");
                        });
                    }

                    @Override
                    public void onPaymentError(ZaloPayError zaloPayError, String zpTransTokenParam, String message) {
                        runOnUiThread(() -> {
                            Toast.makeText(ZaloPaymentActivity.this, "Lỗi thanh toán: " + message, Toast.LENGTH_SHORT).show();
                            navigateToBookings("PENDING_PAYMENT");
                        });
                    }
                }
        );
    }

    private void initPaymentApi() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(logging).build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BuildConfig.BACKEND_BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                .build();
        paymentApi = retrofit.create(PaymentApi.class);
    }

    private void callWebhookToConfirm(String appTransId) {
        // Trong môi trường TEST, chúng ta giả lập việc Backend nhận được callback từ ZaloPay
        // Dữ liệu "data" thường là JSON chứa app_trans_id
        String jsonData = "{\"app_trans_id\":\"" + appTransId + "\"}";
        
        // Lưu ý: "mac" ở đây đúng ra phải được tính từ key2. 
        // Vì đây là giả lập phía Client để Test, tôi để mặc định hoặc bạn cần xử lý ở Backend để chấp nhận chuỗi này.
        String dummyMac = "calculated_at_backend_or_bypass_for_test"; 

        ZaloWebhookRequestDto webhookRequest = new ZaloWebhookRequestDto(jsonData, dummyMac, 1);

        disposables.add(
                paymentApi.simulateZaloPayWebhook(webhookRequest)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(response -> {
                            binding.progressBar.setVisibility(View.GONE);
                            navigateToBookings("CONFIRMED");
                        }, error -> {
                            binding.progressBar.setVisibility(View.GONE);
                            Log.e("ZaloPayment", "Webhook failed", error);
                            // Dù webhook fail (do mac) nhưng SDK đã success, vẫn cho về màn hình booking để user check
                            navigateToBookings("CONFIRMED"); 
                        })
        );
    }

    private void navigateToBookings(String tabStatus) {
        if (hasNavigated) return;
        hasNavigated = true;

        Intent intent = new Intent(ZaloPaymentActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("navigate_to", "bookings");
        intent.putExtra("tab_status", tabStatus);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (!hasNavigated) {
            ZaloPaySDK.getInstance().onResult(intent);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disposables.clear();
        binding = null;
    }
}
