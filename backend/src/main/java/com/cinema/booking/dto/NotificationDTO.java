package com.cinema.booking.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationDTO {
    private Integer notificationId;
    private Integer userId;
    private String title;
    private String message;
    private Boolean isRead;
}
