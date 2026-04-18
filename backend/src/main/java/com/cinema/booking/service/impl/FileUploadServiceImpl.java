package com.cinema.booking.service.impl;

import com.cinema.booking.service.FileUploadService;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Service
public class FileUploadServiceImpl implements FileUploadService {

    @Autowired
    private Cloudinary cloudinary;

    @Override
    public String uploadFile(MultipartFile multipartFile) throws IOException {
        String originalFilename = multipartFile.getOriginalFilename();
        
        // Đặt lại tên File bằng UUID để không bị đụng chạm vỡ File trên Cloud
        String publicId = UUID.randomUUID().toString() + "_" + 
                          (originalFilename != null ? originalFilename.replaceAll("[^a-zA-Z0-9.-]", "_") : "galaxy_file");
        
        // Bắn Lên Cloudinary
        @SuppressWarnings("rawtypes") // Bỏ dòng gạch vàng Warning của Java
        Map uploadResult = cloudinary.uploader().upload(
            multipartFile.getBytes(), 
            ObjectUtils.asMap("public_id", publicId)
        );
        
        // Trích cái Link File vừa đẩy lên thành công nhả về
        return uploadResult.get("url").toString();
    }
}
