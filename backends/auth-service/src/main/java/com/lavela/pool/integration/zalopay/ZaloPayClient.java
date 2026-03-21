package com.lavela.pool.integration.zalopay;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lavela.pool.dto.request.ZaloPayCallbackRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;

/**
 * Minimal ZaloPay client for:
 * - Create order (POST /v2/create) using key1 (HMACSHA256)
 * - Verify callback MAC using key2 (HMACSHA256)
 *
 * App-to-App flow: backend returns zp_trans_token to client, client calls ZaloPay SDK.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ZaloPayClient {

    private static final ZoneId VN_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    private final ObjectMapper objectMapper;

    @Value("${app.zalopay.sandbox.app-id:}")
    private String appId;

    @Value("${app.zalopay.sandbox.key1:}")
    private String key1;

    @Value("${app.zalopay.sandbox.key2:}")
    private String key2;

    @Value("${app.zalopay.sandbox.create-url:https://sb-openapi.zalopay.vn/v2/create}")
    private String createUrl;

    @Value("${app.zalopay.sandbox.query-url:https://sb-openapi.zalopay.vn/v2/query}")
    private String queryUrl;

    @Value("${app.zalopay.sandbox.callback-url:http://localhost:8080/webhooks/payment/ZALOPAY}")
    private String callbackUrl;

    @Value("${app.zalopay.sandbox.bank-code:zalopayapp}")
    private String bankCode;

    private static String toHexLower(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format(Locale.ROOT, "%02x", b));
        }
        return sb.toString();
    }

    private static String hmacSha256Hex(String key, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);
            byte[] raw = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return toHexLower(raw);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to compute HMACSHA256", e);
        }
    }

    public String generateAppTransId() {
        String yymmdd = LocalDate.now(VN_ZONE).format(DateTimeFormatter.ofPattern("yyMMdd"));
        String random = UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase(Locale.ROOT);
        // yymmdd + "_" + 12 chars => safe length
        return yymmdd + "_" + random;
    }

    public ZaloPayCreateOrderResult createOrder(long amountVnd, String appUser) {
        assertConfigured();

        String appTransId = generateAppTransId();
        return createOrderWithAppTransId(amountVnd, appUser, appTransId);
    }

    public ZaloPayCreateOrderResult createOrderWithAppTransId(long amountVnd, String appUser, String appTransId) {
        assertConfigured();

        long appTime = System.currentTimeMillis();

        String embedData = "{}";
        String item = "[]";
        String description = "Merchant pay for order #" + appTransId;

        // hmac_input (per lab + create-order spec without paymentCodeRaw):
        // app_id|app_trans_id|app_user|amount|app_time|embed_data|item
        String hmacInput = String.join("|",
                appId,
                appTransId,
                appUser,
                String.valueOf(amountVnd),
                String.valueOf(appTime),
                embedData,
                item
        );

        String mac = hmacSha256Hex(key1, hmacInput);

        // Build form-urlencoded request
        String form = formEncode("app_id", appId)
                + "&" + formEncode("app_user", appUser)
                + "&" + formEncode("app_time", String.valueOf(appTime))
                + "&" + formEncode("amount", String.valueOf(amountVnd))
                + "&" + formEncode("app_trans_id", appTransId)
                + "&" + formEncode("embed_data", embedData)
                + "&" + formEncode("item", item)
                + "&" + formEncode("bank_code", bankCode)
                + "&" + formEncode("description", description)
                + "&" + formEncode("mac", mac)
                + "&" + formEncode("callback_url", callbackUrl);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(createUrl))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(form))
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            String body = response.body();
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                log.warn("ZaloPay createOrder failed: status={}, body={}", response.statusCode(), body);
                throw new IllegalStateException("ZaloPay createOrder HTTP error: " + response.statusCode());
            }

            JsonNode root = objectMapper.readTree(body);
            int returnCode = root.path("return_code").asInt();
            String returnMessage = root.path("return_message").asText(null);

            String zpTransToken = root.path("zp_trans_token").asText(null);
            String zpTransId = root.path("zp_trans_id").asText(null);
            String orderUrl = root.path("order_url").asText(null);

            if (returnCode != 1) {
                throw new IllegalStateException("ZaloPay createOrder failed: " + returnMessage);
            }

            return ZaloPayCreateOrderResult.builder()
                    .returnCode(returnCode)
                    .returnMessage(returnMessage)
                    .zpTransToken(zpTransToken)
                    .zpTransId(zpTransId)
                    .orderUrl(orderUrl)
                    .build();
        } catch (Exception e) {
            throw new IllegalStateException("ZaloPay createOrder parse/HTTP error", e);
        }
    }

    /**
     * Verify callback MAC and extract app_trans_id from callback.data.
     *
     * ZaloPay callback request schema:
     * {
     *   "data": "<json string>",
     *   "mac": "<hmac hex>",
     *   "type": 1
     * }
     */
    public String verifyCallbackAndExtractAppTransId(ZaloPayCallbackRequest callback) {
        assertConfigured();

        if (callback == null) {
            throw new IllegalArgumentException("ZaloPay callback body is missing");
        }
        String rawData = callback.getData();
        String rawMac = callback.getMac();
        if (!StringUtils.hasText(rawData)) {
            throw new IllegalArgumentException("ZaloPay callback `data` is required (non-empty JSON string)");
        }
        if (!StringUtils.hasText(rawMac)) {
            throw new IllegalArgumentException("ZaloPay callback `mac` is required (HMAC-SHA256 hex, 64 chars)");
        }

        // HMAC is computed on exact `data` payload; trim only outer whitespace (ZaloPay sends compact JSON).
        String dataPayload = rawData.trim();
        // Copy/paste often breaks hex into two lines — remove all whitespace before compare.
        String macNormalized = rawMac.replaceAll("\\s+", "").toLowerCase(Locale.ROOT);

        String expectedMac = hmacSha256Hex(key2, dataPayload);
        if (!expectedMac.equals(macNormalized)) {
            throw new IllegalArgumentException("ZaloPay callback MAC mismatch (check key2 and that `data` matches MAC input exactly)");
        }

        try {
            JsonNode dataRoot = objectMapper.readTree(dataPayload);
            String appTransId = dataRoot.path("app_trans_id").asText(null);
            if (appTransId == null || appTransId.isBlank()) {
                throw new IllegalStateException("ZaloPay callback missing app_trans_id");
            }
            return appTransId.trim();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse ZaloPay callback data JSON", e);
        }
    }

    /**
     * Query order status for reconciliation (order-query).
     * Used to handle callback that does not include explicit success/failure state.
     */
    public ZaloPayQueryOrderResult queryOrder(String appTransId) {
        assertConfigured();

        String appIdTrim = appId.trim();

        // Per ZaloPay Integration Doc §3 Query Order:
        // mac = HMAC(mackey, app_id + "|" + app_trans_id + "|" + mackey)
        // Sandbox/prod: mackey for query is Key2 (same family as callbackkey in callback section).
        String hmacInput = appIdTrim + "|" + appTransId + "|" + key2;
        String mac = hmacSha256Hex(key2, hmacInput);

        try {
            // Official PDF examples send app_id as JSON string, not number — avoid HMAC/parse mismatches.
            var body = objectMapper.createObjectNode();
            body.put("app_id", appIdTrim);
            body.put("app_trans_id", appTransId);
            body.put("mac", mac);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(queryUrl))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            String respBody = response.body();
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("ZaloPay queryOrder HTTP error: " + response.statusCode());
            }

            JsonNode root = objectMapper.readTree(respBody);
            int returnCode = root.path("return_code").asInt();
            String returnMessage = root.path("return_message").asText(null);
            Integer subReturnCode = root.hasNonNull("sub_return_code") ? root.path("sub_return_code").asInt() : null;
            String subReturnMessage = root.path("sub_return_message").asText(null);
            boolean zpProcessing = root.path("is_processing").asBoolean(false);

            return ZaloPayQueryOrderResult.builder()
                    .returnCode(returnCode)
                    .returnMessage(returnMessage)
                    .subReturnCode(subReturnCode)
                    .subReturnMessage(subReturnMessage)
                    .zpProcessingFlag(zpProcessing)
                    .build();
        } catch (Exception e) {
            throw new IllegalStateException("ZaloPay queryOrder failed", e);
        }
    }

    private void assertConfigured() {
        if (appId == null || appId.isBlank()
                || key1 == null || key1.isBlank()
                || key2 == null || key2.isBlank()) {
            throw new IllegalStateException("ZaloPay sandbox is not configured (app-id/key1/key2 missing)");
        }
    }

    private static String formEncode(String key, String value) {
        return urlEncode(key) + "=" + urlEncode(value);
    }

    private static String urlEncode(String value) {
        try {
            return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            // UTF-8 always supported
            throw new IllegalStateException(e);
        }
    }
}

