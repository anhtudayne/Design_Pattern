package com.cinema.booking.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class AuthEntryPointJwt implements AuthenticationEntryPoint {

    private static final Logger logger = LoggerFactory.getLogger(AuthEntryPointJwt.class);

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {
        // Bắt mọi trường hợp cố gắng truy cập API bằng 1 cái JWT giả hoặc expired
        logger.error("Hành vi xâm nhập trái phép (Unauthorized): {}", authException.getMessage());

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        response.getWriter().write("{\n" +
                "  \"status\": 401,\n" +
                "  \"error\": \"Unauthorized - Lỗi xác thực JWT Token\",\n" +
                "  \"message\": \"" + authException.getMessage() + "\",\n" +
                "  \"path\": \"" + request.getServletPath() + "\"\n" +
                "}");
    }
}
