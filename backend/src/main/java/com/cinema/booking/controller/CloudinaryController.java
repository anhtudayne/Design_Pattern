package com.cinema.booking.controller;

import com.cinema.booking.service.CloudinaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/cloudinary")
@Tag(name = "8. Cấp quyền Cloudinary", description = "Các API cấp chữ ký (Signature) để Client upload trực tiếp lên Cloudinary")
public class CloudinaryController {

    @Autowired
    private CloudinaryService cloudinaryService;

    @Operation(summary = "Lấy chữ ký upload (GET)", description = "Cung cấp folder qua query parameter để nhận chữ ký bảo mật từ Cloudinary")
    @GetMapping("/signature")
    public ResponseEntity<Map<String, Object>> getSignature(@RequestParam(required = false) String folder) {
        return ResponseEntity.ok(cloudinaryService.getUploadSignature(folder));
    }

    @Operation(summary = "Lấy thông tin định danh upload (POST)", description = "Cung cấp folder qua request body JSON để nhận đầy đủ credentials (signature, api_key, cloud_name)")
    @PostMapping("/credentials")
    public ResponseEntity<Map<String, Object>> getCredentials(@RequestBody(required = false) Map<String, String> body) {
        String folder = body != null ? body.get("folder") : null;
        return ResponseEntity.ok(cloudinaryService.getUploadSignature(folder));
    }
}
