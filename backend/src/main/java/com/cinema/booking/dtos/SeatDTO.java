package com.cinema.booking.dtos;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeatDTO {
    private Integer seatId;
    private String seatCode;
    private Integer roomId;
    private Integer seatTypeId;
}
