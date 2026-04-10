package com.cinema.booking.dtos;

import com.cinema.booking.entities.Booking.BookingStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingDTO {
    private Integer bookingId;
    private Integer customerId;
    private Integer showtimeId;
    private String promoCode;
    private BigDecimal totalPrice;
    private BookingStatus status;
    private LocalDateTime createdAt;

    private List<TicketLineDTO> tickets;
    private List<FnBLineDTO> fnbs;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TicketLineDTO {
        private Integer ticketId;
        private Integer seatId;
        private String seatCode;
        private String seatRow;
        private Integer seatNumber;
        private String seatType;
        private BigDecimal seatSurcharge;
        private BigDecimal price;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FnBLineDTO {
        private Integer id;
        private Integer itemId;
        private String itemName;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal lineTotal;
    }
}

