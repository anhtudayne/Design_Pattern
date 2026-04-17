package com.cinema.booking.dtos;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromotionDTO {
    private Integer id;
    private String code;
    private String discount_type;
    private BigDecimal discount_value;
    private LocalDateTime valid_to;
}
