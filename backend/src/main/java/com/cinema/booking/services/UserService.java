package com.cinema.booking.services;

import com.cinema.booking.dtos.UserDTO;
import com.cinema.booking.dtos.UserUpdateRequest;

public interface UserService {
    UserDTO getCurrentUser();
    UserDTO updateProfile(UserUpdateRequest request);
}
