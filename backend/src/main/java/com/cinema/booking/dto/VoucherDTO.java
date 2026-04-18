package com.cinema.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VoucherDTO implements Serializable {
    private String code;
    private BigDecimal discountPercentage;
    private BigDecimal maxDiscountAmount;
    private BigDecimal minPurchaseAmount;
    /** TTL in Redis (seconds). */
    private Long ttlSeconds;
}
