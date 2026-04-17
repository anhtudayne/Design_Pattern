package com.cinema.booking.services.strategy_decorator.pricing.decorator;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class DiscountResult {
    private BigDecimal totalDiscount;
    private BigDecimal promotionDiscount;
    private BigDecimal membershipDiscount;
}
