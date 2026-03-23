package com.vaultpool.customer.presentation.ui.payment;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.vaultpool.customer.databinding.ActivityZaloPaymentBinding;
import com.vaultpool.customer.presentation.ui.main.MainActivity;

import vn.zalopay.sdk.listeners.PayOrderListener;
import vn.zalopay.sdk.Environment;
import vn.zalopay.sdk.ZaloPaySDK;
import vn.zalopay.sdk.ZaloPayError;

/**
 * ZaloPay App-to-App payment launcher.
 *
 * Backend returns `redirectUrl = zp_trans_token` for this activity to call:
 * ZaloPaySDK.getInstance().payOrder(..., zp_trans_token, "demozpdk://app", listener)
 */
public class ZaloPaymentActivity extends AppCompatActivity {

    public static final String EXTRA_ZP_TRANS_TOKEN = "extra_zp_trans_token";
    public static final String EXTRA_BOOKING_ID = "extra_booking_id";

    // From lab: AppInfo.APP_ID = 2554
    // If you use production, update this value (ideally via BuildConfig/remote config).
    private static final int ZALOPAY_APP_ID_SANDBOX = 2554;
    private static final String URI_SCHEME = "demozpdk://app";

    private ActivityZaloPaymentBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityZaloPaymentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String zpTransToken = getIntent().getStringExtra(EXTRA_ZP_TRANS_TOKEN);
        if (zpTransToken == null || zpTransToken.isBlank()) {
            Toast.makeText(this, "Thiếu zp_trans_token", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Initialize SDK (SANDBOX for lab)
        ZaloPaySDK.init(ZALOPAY_APP_ID_SANDBOX, Environment.SANDBOX);
        binding.tvStatus.setText("Đang mở ZaloPay...");

        // Launch payment flow
        ZaloPaySDK.getInstance().payOrder(
                this,
                zpTransToken,
                URI_SCHEME,
                new PayOrderListener() {
                    @Override
                    public void onPaymentSucceeded(String transactionId, String transToken, String appTransId) {
                        runOnUiThread(() -> {
                            binding.tvStatus.setText("Thanh toán thành công...");
                            navigateToBookings("CONFIRMED");
                        });
                    }

                    @Override
                    public void onPaymentCanceled(String zpTransTokenParam, String message) {
                        runOnUiThread(() -> {
                            binding.tvStatus.setText("Bạn đã hủy thanh toán.");
                            navigateToBookings("PENDING_PAYMENT");
                        });
                    }

                    @Override
                    public void onPaymentError(ZaloPayError zaloPayError, String zpTransTokenParam, String message) {
                        runOnUiThread(() -> {
                            navigateToBookings("PENDING_PAYMENT");
                        });
                    }

                    private void navigateToBookings(String tabStatus) {
                        Intent intent = new Intent(ZaloPaymentActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        intent.putExtra("navigate_to", "bookings");
                        intent.putExtra("tab_status", tabStatus);
                        startActivity(intent);
                        finish();
                    }
                }
        );
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        // Let ZaloPaySDK handle deep link results
        ZaloPaySDK.getInstance().onResult(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}

