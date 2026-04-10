package com.cinema.booking.controllers;

import com.cinema.booking.dtos.JwtResponse;
import com.cinema.booking.dtos.LoginRequest;
import com.cinema.booking.dtos.MessageResponse;
import com.cinema.booking.dtos.SignupRequest;
import com.cinema.booking.services.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            JwtResponse response = authService.authenticateUser(loginRequest);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println(">>> [AuthService] Login failed for " + loginRequest.getEmail() + ": " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(new MessageResponse("Sai thông tin đăng nhập: " + e.getMessage()));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        try {
            authService.registerUser(signUpRequest);
            return ResponseEntity.ok(new MessageResponse("Tạo thành công Tài khoản! Chào mừng đến với Hệ sinh thái Galaxy Cinema."));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Lỗi: " + e.getMessage()));
        }
    }

    @PostMapping("/google-login")
    public ResponseEntity<?> googleLogin(@RequestBody java.util.Map<String, String> request) {
        try {
            String idToken = request.get("idToken");
            JwtResponse response = authService.googleLogin(idToken);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Google Login Error: " + e.getMessage()));
        }
    }
}
