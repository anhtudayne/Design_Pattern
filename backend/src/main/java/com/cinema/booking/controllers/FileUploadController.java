package com.cinema.booking.controllers;

import com.cinema.booking.services.FileUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/upload")
public class FileUploadController {

    @Autowired
    private FileUploadService fileUploadService;

    // POST /api/upload
    // Chế độ nhận Body: form-data, trường 'file' (MultipartFile)
    @PostMapping
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            // Đẩy vô máy bơm Service
            String imageUrl = fileUploadService.uploadFile(file);
            
            // Lấy được link thì bọc JSON lại nhả ra
            Map<String, String> response = new HashMap<>();
            response.put("url", imageUrl);
            response.put("message", "Đã chèn ảnh lên Cloudinary thành công rực rỡ!");
            
            return ResponseEntity.ok(response);
            
        } catch (IOException e) {
            return ResponseEntity.badRequest().body("Sập cổng Upload ảnh: " + e.getMessage());
        }
    }
}
