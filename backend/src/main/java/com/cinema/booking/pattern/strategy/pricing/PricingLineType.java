package com.cinema.booking.pattern.strategy.pricing;

/** Line of business contributing to {@link com.cinema.booking.dtos.PriceBreakdownDTO}. */
public enum PricingLineType {
    TICKET,
    FNB,
    TIME_BASED_SURCHARGE
}
