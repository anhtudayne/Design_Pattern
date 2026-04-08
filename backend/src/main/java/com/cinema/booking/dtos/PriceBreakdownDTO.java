package com.cinema.booking.dtos;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class PriceBreakdownDTO {
    private BigDecimal ticketTotal;
    private BigDecimal fnbTotal;
    private BigDecimal discountAmount;
    private BigDecimal finalTotal;
}
