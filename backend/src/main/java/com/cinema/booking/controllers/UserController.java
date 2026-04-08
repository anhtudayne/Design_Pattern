package com.cinema.booking.controllers;

import com.cinema.booking.dtos.UserDTO;
import com.cinema.booking.dtos.UserUpdateRequest;
import com.cinema.booking.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/users")
@Tag(name = "11. Người dùng", description = "API quản lý thông tin người dùng")
public class UserController {

    @Autowired
    private UserService userService;

    @Operation(summary = "Lấy thông tin cá nhân", description = "Lấy thông tin của người dùng hiện tại đang đăng nhập")
    @GetMapping("/me")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('MANAGER') or hasRole('STAFF')")
    public ResponseEntity<UserDTO> getCurrentUser() {
        return ResponseEntity.ok(userService.getCurrentUser());
    }

    @Operation(summary = "Cập nhật thông tin cá nhân", description = "Cho phép người dùng hiện tại tự cập nhật thông tin cá nhân (Họ tên, SĐT)")
    @PutMapping("/me")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('MANAGER') or hasRole('STAFF')")
    public ResponseEntity<UserDTO> updateProfile(@RequestBody UserUpdateRequest request) {
        return ResponseEntity.ok(userService.updateProfile(request));
    }
}
