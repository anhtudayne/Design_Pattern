package com.cinema.booking.dtos;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CinemaDTO {
    private Integer cinemaId;
    private String name;
    private String address;
    private Integer locationId;
}
