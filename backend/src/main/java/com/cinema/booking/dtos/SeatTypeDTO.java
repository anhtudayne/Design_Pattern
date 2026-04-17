package com.cinema.booking.dtos;

import lombok.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeatTypeDTO {
    private Integer seatId;
    private String name;
    private BigDecimal priceSurcharge;
}
