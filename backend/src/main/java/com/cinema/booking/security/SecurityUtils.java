package com.cinema.booking.security;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;

public class SecurityUtils {
    public static String hmacSha256(String data, String key) throws Exception {
        byte[] byteKey = key.getBytes(StandardCharsets.UTF_8);
        final String HMAC_SHA256 = "HmacSHA256";
        Mac sha256_HMAC = Mac.getInstance(HMAC_SHA256);
        SecretKeySpec secret_key = new SecretKeySpec(byteKey, HMAC_SHA256);
        sha256_HMAC.init(secret_key);
        return toHexString(sha256_HMAC.doFinal(data.getBytes(StandardCharsets.UTF_8)));
    }

    private static String toHexString(byte[] bytes) {
        return HexFormat.of().formatHex(bytes);
    }
}
