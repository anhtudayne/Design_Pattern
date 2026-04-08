package com.cinema.booking.services;

import java.util.Map;

public interface CloudinaryService {
    Map<String, Object> getUploadSignature(String folder);
}
