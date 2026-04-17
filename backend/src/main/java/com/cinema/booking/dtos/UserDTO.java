package com.cinema.booking.dtos;

import com.cinema.booking.entities.Customer;
import com.cinema.booking.entities.User;
import com.cinema.booking.entities.UserAccount;
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
        UserAccount acc = user.getUserAccount();
        String email = acc != null ? acc.getEmail() : null;
        // user_accounts no longer has created_at in strict schema mode.
        LocalDateTime createdAt = null;

        BigDecimal spending = null;
        Integer loyalty = null;
        String tierName = null;
        if (user instanceof Customer c) {
            // Strict entity model removed membership fields from Customer.
            spending = null;
            loyalty = null;
            tierName = "USER";
        }

        return UserDTO.builder()
                .userId(user.getUserId())
                .fullname(user.getFullname())
                .email(email)
                .phone(user.getPhone())
                .role(user.getSpringSecurityRole())
                .totalSpending(spending)
                .loyaltyPoints(loyalty)
                .createdAt(createdAt)
                .tierName(tierName)
                .build();
    }
}
