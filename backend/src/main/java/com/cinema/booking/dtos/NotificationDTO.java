package com.cinema.booking.dtos;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationDTO {
    private Integer notification_ID;
    private String title;
    private String message;
    private Integer user_ID;
}
