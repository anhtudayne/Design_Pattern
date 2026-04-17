package com.cinema.booking.dtos;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeatDTO {
    private Integer id;
    private String seat_code;
    private Integer room_id;
    private Integer seat_type_id;
}
