package com.cinema.booking.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceBreakdownDTO {
    private BigDecimal ticketTotal;
    private BigDecimal occupancySurcharge;
    private BigDecimal fnbTotal;
    private BigDecimal membershipDiscount;
    private BigDecimal discountAmount;      // voucher discount
    private String appliedStrategy;
    private BigDecimal finalTotal;
}
