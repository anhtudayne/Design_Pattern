package com.cinema.booking.dtos;

import lombok.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeatTypeDTO {
    private Integer seat_ID;
    private String name;
    private BigDecimal price_surcharge;
}
