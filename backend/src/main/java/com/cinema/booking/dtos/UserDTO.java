package com.cinema.booking.dtos;

import com.cinema.booking.entities.User;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {
    private Integer userId;
    private String fullname;
    private String email;
    private String phone;
    private String role;
    private BigDecimal totalSpending;
    private Integer loyaltyPoints;
    private LocalDateTime createdAt;
    private String tierName;

    public static UserDTO fromEntity(User user) {
        return UserDTO.builder()
                .userId(user.getUserId())
                .fullname(user.getFullname())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole().name())
                .totalSpending(user.getTotalSpending())
                .loyaltyPoints(user.getLoyaltyPoints())
                .createdAt(user.getCreatedAt())
                .tierName(user.getTier() != null ? user.getTier().getName() : "Vô danh")
                .build();
    }
}
