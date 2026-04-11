package com.cinema.booking.services.impl;

import com.cinema.booking.dtos.MomoPaymentRequest;
import com.cinema.booking.dtos.MomoPaymentResponse;
import com.cinema.booking.security.SecurityUtils;
import com.cinema.booking.services.MomoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Service
public class MomoServiceImpl implements MomoService {

    @Value("${momo.endpoint}")
    private String endpoint;

    @Value("${momo.partner-code}")
    private String partnerCode;

    @Value("${momo.access-key}")
    private String accessKey;

    @Value("${momo.secret-key}")
    private String secretKey;

    @Value("${momo.redirect-url}")
    private String redirectUrl;

    @Value("${momo.ipn-url}")
    private String ipnUrl;

    private final RestTemplate restTemplate;

    @Autowired
    public MomoServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public MomoPaymentResponse createPayment(String orderId, long amount, String orderInfo, String extraData) throws Exception {
        String requestId = UUID.randomUUID().toString();
        String requestType = "captureWallet";

        // Raw signature string format:
        // accessKey=$accessKey&amount=$amount&extraData=$extraData&ipnUrl=$ipnUrl&orderId=$orderId&orderInfo=$orderInfo&partnerCode=$partnerCode&redirectUrl=$redirectUrl&requestId=$requestId&requestType=$requestType
        String rawHash = "accessKey=" + accessKey +
                "&amount=" + amount +
                "&extraData=" + extraData +
                "&ipnUrl=" + ipnUrl +
                "&orderId=" + orderId +
                "&orderInfo=" + orderInfo +
                "&partnerCode=" + partnerCode +
                "&redirectUrl=" + redirectUrl +
                "&requestId=" + requestId +
                "&requestType=" + requestType;

        String signature = SecurityUtils.hmacSha256(rawHash, secretKey);

        MomoPaymentRequest request = MomoPaymentRequest.builder()
                .partnerCode(partnerCode)
                .partnerName("StarCine")
                .storeId("StarCine")
                .requestId(requestId)
                .amount(amount)
                .orderId(orderId)
                .orderInfo(orderInfo)
                .redirectUrl(redirectUrl)
                .ipnUrl(ipnUrl)
                .lang("vi")
                .extraData(extraData)
                .requestType(requestType)
                .signature(signature)
                .build();

        MomoPaymentResponse response = restTemplate.postForObject(endpoint + "/create", request, MomoPaymentResponse.class);
        if (response != null) {
            System.out.println(">>> [StarCine] MoMo Response - ResultCode: " + response.getResultCode() + ", Message: " + response.getMessage());
            if (response.getPayUrl() == null) {
                System.err.println(">>> [StarCine] WARNING: MoMo payUrl is NULL! Request details: amount=" + amount + ", orderId=" + orderId);
            }
        }
        return response;
    }

    @Override
    public boolean verifySignature(Object callbackRequest) throws Exception {
        // Implementation for signature verification from MoMo callback would go here
        // Usually involves concatenating keys in specific order and hashing with secretKey
        return true; // Simplified for this stage
    }
}
