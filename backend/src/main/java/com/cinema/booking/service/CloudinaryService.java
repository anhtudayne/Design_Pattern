package com.cinema.booking.service;

import java.util.Map;

public interface CloudinaryService {
    Map<String, Object> getUploadSignature(String folder);
}
