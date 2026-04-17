package com.cinema.booking.dtos;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ShowtimeDTO {
    private Integer showtime_ID;
    private Integer movie_id;
    private Integer room_id;
    private LocalDateTime start_time;
    private LocalDateTime end_time;
    private BigDecimal basePrice;
}
