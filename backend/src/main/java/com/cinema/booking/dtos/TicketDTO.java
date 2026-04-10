package com.cinema.booking.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketDTO {
    private Integer ticketId;
    private Integer bookingId;
    private String movieTitle;
    private LocalDateTime showtimeDate;
    private String roomName;
    private String seatCode;
    private String seatRow;
    private Integer seatNumber;
    private String seatType;
    private BigDecimal price;
    private String bookingCode;
}
