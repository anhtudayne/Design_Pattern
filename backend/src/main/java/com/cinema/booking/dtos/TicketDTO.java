package com.cinema.booking.dtos;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketDTO {
    private Integer ticketId;
    private Integer movieId;
    private Integer showtimeId;
    private Integer seatId;
    private BigDecimal unitPrice;
    private LocalDateTime holdExpiresAt;
    private Integer bookingId;
}
