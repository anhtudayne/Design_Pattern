package com.cinema.booking.dtos;

import com.cinema.booking.entities.Payment.PaymentStatus;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentDTO {
    private Integer paymentId;
    private Integer bookingId;
    private String paymentMethod;
    private BigDecimal amount;
    private LocalDateTime paidAt;
    private PaymentStatus status;
}
