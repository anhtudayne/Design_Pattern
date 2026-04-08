package com.cinema.booking.services;

import com.cinema.booking.dtos.JwtResponse;
import com.cinema.booking.dtos.LoginRequest;
import com.cinema.booking.dtos.SignupRequest;

public interface AuthService {
    JwtResponse authenticateUser(LoginRequest loginRequest);
    void registerUser(SignupRequest signUpRequest);
    JwtResponse googleLogin(String idTokenString);
}
