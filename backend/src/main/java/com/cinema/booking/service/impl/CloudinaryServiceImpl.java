package com.cinema.booking.service.impl;

import com.cinema.booking.service.CloudinaryService;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class CloudinaryServiceImpl implements CloudinaryService {

    @Autowired
    private Cloudinary cloudinary;

    @Value("${cloudinary.api_key}")
    private String apiKey;

    @Value("${cloudinary.cloud_name}")
    private String cloudName;

    @Override
    public Map<String, Object> getUploadSignature(String folder) {
        long timestamp = System.currentTimeMillis() / 1000;
        
        Map<String, Object> params = new HashMap<>();
        params.put("timestamp", timestamp);
        if (folder != null && !folder.isEmpty()) {
            params.put("folder", folder);
        }

        // Tạo chữ ký (Signature) từ Cloudinary SDK
        String signature = cloudinary.apiSignRequest(params, cloudinary.config.apiSecret);

        Map<String, Object> response = new HashMap<>();
        response.put("signature", signature);
        response.put("timestamp", timestamp);
        response.put("api_key", apiKey);
        response.put("cloud_name", cloudName);
        if (folder != null && !folder.isEmpty()) {
            response.put("folder", folder);
        }

        return response;
    }
}
