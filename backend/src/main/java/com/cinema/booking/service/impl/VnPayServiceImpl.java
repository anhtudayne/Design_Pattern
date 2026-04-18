package com.cinema.booking.service.impl;

import com.cinema.booking.dto.response.VnpayPaymentResponse;
import com.cinema.booking.service.VnPayService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.TreeMap;

/**
 * Tạo URL thanh toán VNPay (sandbox/prod). Khi {@code vnpay.enabled=false} hoặc thiếu hash secret — trả URL demo.
 */
@Service
@Slf4j
public class VnPayServiceImpl implements VnPayService {

    private static final DateTimeFormatter VNP_DATE = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    @Value("${vnpay.enabled:false}")
    private boolean enabled;

    @Value("${vnpay.payment-url}")
    private String paymentUrl;

    @Value("${vnpay.tmn-code:}")
    private String tmnCode;

    @Value("${vnpay.hash-secret:}")
    private String hashSecret;

    @Value("${vnpay.return-url:${app.frontend-url}/profile/transactions}")
    private String returnUrl;

    @Override
    public VnpayPaymentResponse createPayment(String orderId, long amountVnd, String orderInfo, String extraData)
            throws Exception {
        VnpayPaymentResponse response = new VnpayPaymentResponse();

        if (!enabled || hashSecret == null || hashSecret.isBlank() || tmnCode == null || tmnCode.isBlank()) {
            String demo = paymentUrl + "?demo=1&vnp_TxnRef=" + URLEncoder.encode(orderId, StandardCharsets.UTF_8)
                    + "&vnp_OrderInfo=" + URLEncoder.encode(orderInfo != null ? orderInfo : "", StandardCharsets.UTF_8);
            response.setPayUrl(demo);
            log.warn("[VNPay] enabled=false hoặc thiếu tmn/hash — trả URL demo.");
            return response;
        }

        String createDate = LocalDateTime.now().format(VNP_DATE);
        long amountMinor = amountVnd * 100L;

        TreeMap<String, String> params = new TreeMap<>();
        params.put("vnp_Version", "2.1.0");
        params.put("vnp_Command", "pay");
        params.put("vnp_TmnCode", tmnCode);
        params.put("vnp_Locale", "vn");
        params.put("vnp_CurrCode", "VND");
        params.put("vnp_TxnRef", orderId);
        params.put("vnp_OrderInfo", orderInfo != null ? orderInfo : "Payment");
        params.put("vnp_OrderType", "other");
        params.put("vnp_Amount", String.valueOf(amountMinor));
        params.put("vnp_ReturnUrl", returnUrl);
        params.put("vnp_CreateDate", createDate);
        params.put("vnp_IpAddr", "127.0.0.1");

        StringBuilder hashData = new StringBuilder();
        for (Map.Entry<String, String> e : params.entrySet()) {
            String k = e.getKey();
            String v = e.getValue();
            if (v != null && !v.isEmpty()) {
                hashData.append(k).append('=').append(v).append('&');
            }
        }
        String signData = hashData.length() > 0 ? hashData.substring(0, hashData.length() - 1) : "";
        String secureHash = hmacSHA512(hashSecret, signData);

        StringBuilder url = new StringBuilder(paymentUrl).append('?');
        for (Map.Entry<String, String> e : params.entrySet()) {
            String k = e.getKey();
            String v = e.getValue();
            if (v != null && !v.isEmpty()) {
                url.append(k).append('=').append(URLEncoder.encode(v, StandardCharsets.UTF_8)).append('&');
            }
        }
        url.append("vnp_SecureHash=").append(secureHash);
        response.setPayUrl(url.toString());
        return response;
    }

    private static String hmacSHA512(String key, String data) throws Exception {
        Mac hmac = Mac.getInstance("HmacSHA512");
        SecretKeySpec secret = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
        hmac.init(secret);
        byte[] raw = hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : raw) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
