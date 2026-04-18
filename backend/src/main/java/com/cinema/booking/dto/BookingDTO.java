package com.cinema.booking.dto;

import com.cinema.booking.entity.Booking.BookingStatus;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingDTO {
    private Integer bookingId;
    private String bookingCode;
    private Integer userId;
    private String customerName;
    private String customerPhone;
    private Integer promotionId;
    private BookingStatus status;
    private LocalDateTime createdAt;
    private List<TicketLineDTO> ticketList;
    private List<FnBLineDTO> fnbLines;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TicketLineDTO {
        private Integer ticketId;
        private Integer movieId;
        private Integer showtimeId;
        private Integer seatId;
        private String seatCode;
        private String seatType;
        private java.math.BigDecimal unitPrice;
        private LocalDateTime holdExpiresAt;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FnBLineDTO {
        private Integer id;
        private Integer fnbItemId;
        private String itemName;
        private java.math.BigDecimal unitPrice;
        private Integer quantity;
    }
}
