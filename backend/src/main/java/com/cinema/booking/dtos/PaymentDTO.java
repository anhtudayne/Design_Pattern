package com.cinema.booking.dtos;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentDTO {
    private Integer id;
    private Integer booking_id;
    private String payment_method;
    private BigDecimal amount;
    private LocalDateTime paid_at;
    private String status;
}
