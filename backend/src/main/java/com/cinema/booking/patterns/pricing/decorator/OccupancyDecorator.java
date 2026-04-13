package com.cinema.booking.patterns.pricing.decorator;

import com.cinema.booking.patterns.pricing.context.PricingContext;
import com.cinema.booking.patterns.specification.PricingConditions;

import java.math.BigDecimal;

/**
 * Decorator — thêm phụ thu lấp đầy (occupancy surcharge) vào ticketTotal.
 * Chỉ áp dụng khi tỷ lệ lấp đầy vượt ngưỡng {@link #HIGH_OCCUPANCY_THRESHOLD}%.
 */
public class OccupancyDecorator extends AbstractPriceCalculatorDecorator {

    static final int HIGH_OCCUPANCY_THRESHOLD = 80;
    static final BigDecimal OCCUPANCY_SURCHARGE_PCT = new BigDecimal("10");

    public OccupancyDecorator(PriceCalculator inner) {
        super(inner);
    }

    @Override
    protected void decorate(PricingAccumulator acc, PricingContext ctx) {
        if (PricingConditions.isHighOccupancy(HIGH_OCCUPANCY_THRESHOLD).test(ctx)) {
            BigDecimal surcharge = acc.getTicketTotal()
                    .multiply(OCCUPANCY_SURCHARGE_PCT)
                    .divide(new BigDecimal("100"));
            acc.setOccupancySurcharge(surcharge);
        }
    }
}
