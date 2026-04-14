package com.cinema.booking.services.strategy_decorator.pricing;

import com.cinema.booking.dtos.PriceBreakdownDTO;

/**
 * Interface cho pricing engine — cho phép CachingPricingEngineProxy wrap PricingEngine
 * theo Proxy pattern mà không ảnh hưởng đến caller.
 */
public interface IPricingEngine {
    PriceBreakdownDTO calculateTotalPrice(PricingContext context);
}
