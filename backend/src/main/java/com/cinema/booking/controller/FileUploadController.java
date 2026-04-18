package com.cinema.booking.controller;

import com.cinema.booking.service.FileUploadService;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class FileUploadController {

    @Autowired
    private FileUploadService fileUploadService;

    // POST /api/upload
    // Chế độ nhận Body: form-data, trường 'file' (MultipartFile)
    @PostMapping
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file) throws IOException {
        // Đẩy vô máy bơm Service
        String imageUrl = fileUploadService.uploadFile(file);
        
        // Lấy được link thì bọc JSON lại nhả ra
        Map<String, String> response = new HashMap<>();
        response.put("url", imageUrl);
        response.put("message", "Đã chèn ảnh lên Cloudinary thành công rực rỡ!");
        
        return ResponseEntity.ok(response);
    }
}
