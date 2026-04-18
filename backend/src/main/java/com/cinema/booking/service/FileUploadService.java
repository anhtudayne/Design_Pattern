package com.cinema.booking.service;

import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

public interface FileUploadService {
    // Nhận vào 1 đối tượng ảnh, nhả ra 1 cái link ảnh trên Cloud
    String uploadFile(MultipartFile multipartFile) throws IOException;
}
