package com.cinema.booking.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomDTO {
    private Integer roomId;
    private String name;
    private Integer cinemaId;
    private String screenType;
}
