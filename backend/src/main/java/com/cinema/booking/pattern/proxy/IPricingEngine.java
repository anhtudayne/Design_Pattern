package com.cinema.booking.pattern.proxy;

import com.cinema.booking.dto.PriceBreakdownDTO;
import com.cinema.booking.pattern.strategy.pricing.PricingContext;

/**
 * Interface cho pricing engine — cho phép CachingPricingEngineProxy wrap PricingEngine
 * theo Proxy pattern mà không ảnh hưởng đến caller.
 */
public interface IPricingEngine {
    PriceBreakdownDTO calculateTotalPrice(PricingContext context);
}
