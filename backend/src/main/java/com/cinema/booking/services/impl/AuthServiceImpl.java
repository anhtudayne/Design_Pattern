package com.cinema.booking.services.impl;

import com.cinema.booking.services.AuthService;

import com.cinema.booking.dtos.JwtResponse;
import com.cinema.booking.dtos.LoginRequest;
import com.cinema.booking.dtos.SignupRequest;
import com.cinema.booking.entities.Customer;
import com.cinema.booking.entities.UserAccount;
import com.cinema.booking.repositories.UserAccountRepository;
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

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    public JwtResponse authenticateUser(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
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
        if (userAccountRepository.existsByEmail(signUpRequest.getEmail())) {
            throw new RuntimeException("Email đã được sử dụng trên hệ thống!");
        }

        Customer customer = new Customer();
        customer.setFullname(signUpRequest.getFullname());
        customer.setPhone(signUpRequest.getPhone());

        UserAccount account = UserAccount.builder()
                .email(signUpRequest.getEmail())
                .passwordHash(encoder.encode(signUpRequest.getPassword()))
                .build();

        account.setUser(customer);
        customer.setUserAccount(account);

        userRepository.save(customer);

        try {
            emailService.sendWelcomeEmail(account.getEmail(), customer.getFullname());
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

                UserAccount account = userAccountRepository.findByEmail(email).orElseGet(() -> {
                    Customer newUser = new Customer();
                    newUser.setFullname(name != null ? name : email);
                    UserAccount ua = UserAccount.builder()
                            .email(email)
                            .passwordHash(encoder.encode(java.util.UUID.randomUUID().toString()))
                            .build();
                    ua.setUser(newUser);
                    newUser.setUserAccount(ua);
                    userRepository.save(newUser);
                    return newUser.getUserAccount();
                });

                String jwt = jwtUtils.generateTokenFromUsername(account.getEmail());

                return new JwtResponse(jwt, account.getUser().getUserId(), account.getEmail(),
                        java.util.Collections.singletonList("ROLE_" + account.getUser().getSpringSecurityRole()));
            } else {
                throw new RuntimeException("Xác thực Google thất bại!");
            }
        } catch (Exception e) {
            throw new RuntimeException("Lỗi đăng nhập Google: " + e.getMessage());
        }
    }
}
