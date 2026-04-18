package com.cinema.booking.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class PaymentHistoryDTO {
    private Integer paymentId;
    private String paymentMethod;
    private BigDecimal amount;
    private String status;
    private LocalDateTime paidAt;
    private BookingSummary booking;

    @Data
    @Builder
    public static class BookingSummary {
        private Integer bookingId;
        private String bookingCode;
        private ShowtimeSummary showtime;
    }

    @Data
    @Builder
    public static class ShowtimeSummary {
        private MovieSummary movie;
    }

    @Data
    @Builder
    public static class MovieSummary {
        private String title;
        private String posterUrl;
    }
}
