package com.cinema.booking.service.impl;

import com.cinema.booking.dto.request.MomoCallbackRequest;
import com.cinema.booking.dto.MomoPaymentResponse;
import com.cinema.booking.service.MomoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Tích hợp MoMo Partner API (create) — có chế độ {@code momo.dev-skip-external} để không gọi ra ngoài khi dev.
 */
@Service
@Slf4j
public class MomoServiceImpl implements MomoService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${momo.endpoint}")
    private String endpoint;

    @Value("${momo.access-key}")
    private String accessKey;

    @Value("${momo.partner-code}")
    private String partnerCode;

    @Value("${momo.secret-key}")
    private String secretKey;

    @Value("${momo.redirect-url}")
    private String redirectUrl;

    @Value("${momo.ipn-url}")
    private String ipnUrl;

    @Value("${momo.dev-skip-external:false}")
    private boolean devSkipExternal;

    public MomoServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public MomoPaymentResponse createPayment(String orderId, long amountVnd, String orderInfo, String extraData)
            throws Exception {
        if (devSkipExternal) {
            MomoPaymentResponse mock = new MomoPaymentResponse();
            mock.setPayUrl("about:blank#momo-dev-skip-" + orderId);
            log.warn("[MoMo] dev-skip-external=true — không POST tới cổng MoMo.");
            return mock;
        }

        String requestId = UUID.randomUUID().toString();
        String requestType = "captureWallet";
        String rawSignature = "accessKey=" + accessKey
                + "&amount=" + amountVnd
                + "&extraData=" + (extraData != null ? extraData : "")
                + "&ipnUrl=" + ipnUrl
                + "&orderId=" + orderId
                + "&orderInfo=" + orderInfo
                + "&partnerCode=" + partnerCode
                + "&redirectUrl=" + redirectUrl
                + "&requestId=" + requestId
                + "&requestType=" + requestType;
        String signature = hmacSha256(rawSignature, secretKey);

        Map<String, Object> body = new HashMap<>();
        body.put("partnerCode", partnerCode);
        body.put("partnerName", "StarCine");
        body.put("storeId", "StarCineStore");
        body.put("requestId", requestId);
        body.put("amount", amountVnd);
        body.put("orderId", orderId);
        body.put("orderInfo", orderInfo);
        body.put("redirectUrl", redirectUrl);
        body.put("ipnUrl", ipnUrl);
        body.put("lang", "vi");
        body.put("extraData", extraData != null ? extraData : "");
        body.put("requestType", requestType);
        body.put("signature", signature);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(endpoint, entity, String.class);
        Map<?, ?> root = objectMapper.readValue(response.getBody(), Map.class);
        MomoPaymentResponse out = new MomoPaymentResponse();
        if (root != null) {
            Object payUrl = root.get("payUrl");
            if (payUrl != null) {
                out.setPayUrl(payUrl.toString());
            }
            Object deeplink = root.get("deeplink");
            if (deeplink != null) {
                out.setDeeplink(deeplink.toString());
            }
            Object qr = root.get("qrCodeUrl");
            if (qr != null) {
                out.setQrCodeUrl(qr.toString());
            }
        }
        if (out.getPayUrl() == null || out.getPayUrl().isBlank()) {
            throw new IllegalStateException("MoMo không trả payUrl: " + response.getBody());
        }
        return out;
    }

    @Override
    public boolean verifySignature(MomoCallbackRequest callback) {
        if (callback == null || callback.getSignature() == null || secretKey == null) {
            return false;
        }
        try {
            String raw = "accessKey=" + accessKey
                    + "&amount=" + nullToEmpty(callback.getAmount())
                    + "&extraData=" + nullToEmpty(callback.getExtraData())
                    + "&message=" + nullToEmpty(callback.getMessage())
                    + "&orderId=" + nullToEmpty(callback.getOrderId())
                    + "&orderInfo=" + nullToEmpty(callback.getOrderInfo())
                    + "&orderType=" + nullToEmpty(callback.getOrderType())
                    + "&partnerCode=" + nullToEmpty(callback.getPartnerCode())
                    + "&payType=" + nullToEmpty(callback.getPayType())
                    + "&requestId=" + nullToEmpty(callback.getRequestId())
                    + "&responseTime=" + nullToEmpty(callback.getResponseTime())
                    + "&resultCode=" + (callback.getResultCode() != null ? callback.getResultCode() : "")
                    + "&transId=" + nullToEmpty(callback.getTransId());
            String expected = hmacSha256(raw, secretKey);
            return expected.equalsIgnoreCase(callback.getSignature());
        } catch (Exception e) {
            log.warn("verifySignature MoMo: {}", e.getMessage());
            return false;
        }
    }

    private static String nullToEmpty(String s) {
        return s != null ? s : "";
    }

    private static String hmacSha256(String data, String key) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] raw = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : raw) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
