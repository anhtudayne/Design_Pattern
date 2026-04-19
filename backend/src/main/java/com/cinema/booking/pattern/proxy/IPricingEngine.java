package com.cinema.booking.pattern.proxy;

import com.cinema.booking.dto.PriceBreakdownDTO;
import com.cinema.booking.pattern.strategy.pricing.PricingContext;

/** Hợp đồng tính giá: engine thật và {@link CachingPricingEngineProxy} đều triển khai. */
public interface IPricingEngine {
    PriceBreakdownDTO calculateTotalPrice(PricingContext context);
}
