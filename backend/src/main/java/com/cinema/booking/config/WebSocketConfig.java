package com.cinema.booking.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Broker nội bộ của Spring — xử lý tất cả topic bắt đầu bằng /topic
        config.enableSimpleBroker("/topic");
        // Prefix cho các message gửi từ Client lên Server (nếu cần 2-chiều sau này)
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Chỉ dùng native WebSocket (không SockJS) để tương thích với Vite/browser
        registry.addEndpoint("/ws").setAllowedOriginPatterns("*");
    }
}
