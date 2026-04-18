package com.cinema.booking.dto;

import com.cinema.booking.entity.Promotion.DiscountType;
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
    private DiscountType discountType;
    private BigDecimal discountValue;
    private LocalDateTime validTo;
}
