package com.cinema.booking.dtos;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ShowtimeDTO {
    private Integer showtimeId;
    private Integer movieId;
    private Integer roomId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BigDecimal basePrice;
}
