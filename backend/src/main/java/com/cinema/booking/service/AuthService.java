package com.cinema.booking.service;

import com.cinema.booking.dto.response.JwtResponse;
import com.cinema.booking.dto.request.LoginRequest;
import com.cinema.booking.dto.request.SignupRequest;

public interface AuthService {
    JwtResponse authenticateUser(LoginRequest loginRequest);
    void registerUser(SignupRequest signUpRequest);
    JwtResponse googleLogin(String idTokenString);
}
