package com.cinema.booking.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatStatusDTO {
    private Integer seatId;
    private String seatCode;
    private String seatRow;
    private Integer seatNumber;
    private String seatType;
    private BigDecimal totalPrice; // base_price + seat_surcharge
    private SeatStatus status;

    public enum SeatStatus {
        VACANT, SOLD, PENDING
    }
}
