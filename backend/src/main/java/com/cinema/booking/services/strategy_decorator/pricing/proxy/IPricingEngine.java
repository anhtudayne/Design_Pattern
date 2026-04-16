package com.cinema.booking.services.strategy_decorator.pricing.proxy;

import com.cinema.booking.dtos.PriceBreakdownDTO;
import com.cinema.booking.services.strategy_decorator.pricing.core.PricingContext;

/**
 * Interface cho pricing engine — cho phép CachingPricingEngineProxy wrap PricingEngine
 * theo Proxy pattern mà không ảnh hưởng đến caller.
 */
public interface IPricingEngine {
    PriceBreakdownDTO calculateTotalPrice(PricingContext context);
}
