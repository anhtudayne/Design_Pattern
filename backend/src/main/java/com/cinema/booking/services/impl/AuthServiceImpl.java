package com.cinema.booking.services.impl;

import com.cinema.booking.services.AuthService;

import com.cinema.booking.dtos.JwtResponse;
import com.cinema.booking.dtos.LoginRequest;
import com.cinema.booking.dtos.SignupRequest;
import com.cinema.booking.entities.User;
import com.cinema.booking.repositories.UserRepository;
import com.cinema.booking.security.JwtUtils;
import com.cinema.booking.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    public JwtResponse authenticateUser(LoginRequest loginRequest) {
        // Gọi lên Spring Security Manager để check mật khẩu do Bcrypt băm
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

        // Đẩy User này vào Ngữ cảnh bảo mật hiện tại của App
        SecurityContextHolder.getContext().setAuthentication(authentication);
        // Bảo JwtUtils đẻ ra 1 token mới
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        return new JwtResponse(jwt,
                userDetails.getId(),
                userDetails.getEmail(),
                roles);
    }

    @Autowired
    private com.cinema.booking.services.EmailService emailService;

    @Override
    @Transactional
    public void registerUser(SignupRequest signUpRequest) {
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            throw new RuntimeException("Email đã được sử dụng trên hệ thống!");
        }

        // Tạo Entity User mới để nén xuống MySQL (Chức vụ mặc định là USER)
        User user = User.builder()
                .fullname(signUpRequest.getFullname())
                .email(signUpRequest.getEmail())
                .passwordHash(encoder.encode(signUpRequest.getPassword()))
                .phone(signUpRequest.getPhone())
                .role(User.Role.USER)
                .createdAt(LocalDateTime.now())
                .build();

        userRepository.save(user);

        // Gửi email chào mừng (Có thể dùng RabbitMQ nếu muốn bất đồng bộ)
        try {
            emailService.sendWelcomeEmail(user.getEmail(), user.getFullname());
        } catch (Exception e) {
            System.err.println(">>> Lỗi khi gọi EmailService: " + e.getMessage());
        }
    }

    @org.springframework.beans.factory.annotation.Value("${OAUTH_CLIENT_ID:no-client-id}")
    private String clientId;

    @Override
    @Transactional
    public JwtResponse googleLogin(String idTokenString) {
        try {
            com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier verifier = 
                new com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier.Builder(
                    new com.google.api.client.http.javanet.NetHttpTransport(), 
                    new com.google.api.client.json.gson.GsonFactory())
                .setAudience(java.util.Collections.singletonList(clientId))
                .build();

            com.google.api.client.googleapis.auth.oauth2.GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken != null) {
                com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload payload = idToken.getPayload();
                String email = payload.getEmail();
                String name = (String) payload.get("name");

                User user = userRepository.findByEmail(email).orElseGet(() -> {
                    User newUser = User.builder()
                            .email(email)
                            .fullname(name)
                            .passwordHash(encoder.encode(java.util.UUID.randomUUID().toString()))
                            .role(User.Role.USER)
                            .createdAt(LocalDateTime.now())
                            .build();
                    return userRepository.save(newUser);
                });

                // Tạo JWT cho User vừa đăng nhập qua Google
                String jwt = jwtUtils.generateTokenFromUsername(user.getEmail());

                return new JwtResponse(jwt, user.getUserId(), user.getEmail(), 
                        java.util.Collections.singletonList(user.getRole().name()));
            } else {
                throw new RuntimeException("Xác thực Google thất bại!");
            }
        } catch (Exception e) {
            throw new RuntimeException("Lỗi đăng nhập Google: " + e.getMessage());
        }
    }
}
