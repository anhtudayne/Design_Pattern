package com.cinema.booking.dtos;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingDTO {
    private Integer id;
    private Integer user_ID;
    private String booking_code;
    private String status;
    private LocalDateTime created_at;
    private List<TicketLineDTO> TicketList;
    private List<FnBLineDTO> fnBLines;
    private Integer promotion_id;
    private Integer payment_id;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TicketLineDTO {
        private Integer id;
        private Integer movie_ID;
        private Integer showtime_ID;
        private Integer seat_ID;
        private BigDecimal unit_price;
        private LocalDateTime hold_expires_at;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FnBLineDTO {
        private Integer id;
        private Integer FnbItem_ID;
        private int quantity;
    }
}
