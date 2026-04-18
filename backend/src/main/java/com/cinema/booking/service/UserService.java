package com.cinema.booking.service;

import com.cinema.booking.dto.UserDTO;
import com.cinema.booking.dto.request.UserUpdateRequest;

public interface UserService {
    UserDTO getCurrentUser();
    UserDTO updateProfile(UserUpdateRequest request);
}
