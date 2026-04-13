package com.cinema.booking.patterns.pricing.validation;

import lombok.Value;

import java.math.BigDecimal;

/**
 * Immutable context truyền vào PriceValidationChain.
 * Được tạo từ PricingAccumulator ngay trước khi validate:
 * <pre>
 *   new PriceValidationContext(
 *       acc.subtotal(), acc.totalDiscount(), acc.finalTotal(), ctx.getSeats().size()
 *   );
 * </pre>
 */
@Value
public class PriceValidationContext {
    BigDecimal subtotal;      // ticketTotal + occupancySurcharge + fnbTotal
    BigDecimal totalDiscount; // membershipDiscount + voucherDiscount
    BigDecimal finalTotal;
    int seatCount;
}
