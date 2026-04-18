package com.cinema.booking.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GenreDTO {
    private Integer genreId;
    private String name;
}
