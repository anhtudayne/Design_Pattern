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
        JwtResponse response = authService.authenticateUser(loginRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        authService.registerUser(signUpRequest);
        return ResponseEntity.ok(new MessageResponse("Tạo thành công Tài khoản! Chào mừng đến với Hệ sinh thái Galaxy Cinema."));
    }

    @PostMapping("/google-login")
    public ResponseEntity<?> googleLogin(@RequestBody java.util.Map<String, String> request) {
        String idToken = request.get("idToken");
        JwtResponse response = authService.googleLogin(idToken);
        return ResponseEntity.ok(response);
    }
}
