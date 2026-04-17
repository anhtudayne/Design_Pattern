package com.cinema.booking.services.strategy_decorator.pricing.strategy;

/** Line of business contributing to {@link com.cinema.booking.dtos.PriceBreakdownDTO}. */
public enum PricingLineType {
    TICKET,
    FNB,
    TIME_BASED_SURCHARGE
}
