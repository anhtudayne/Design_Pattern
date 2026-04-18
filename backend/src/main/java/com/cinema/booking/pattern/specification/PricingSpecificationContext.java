package com.cinema.booking.pattern.specification;
import com.cinema.booking.pattern.specification.PricingSpecificationContext;
import com.cinema.booking.pattern.specification.PricingConditions;
import com.cinema.booking.pattern.strategy.pricing.PricingContext;

import com.cinema.booking.entity.Promotion;
import com.cinema.booking.entity.Seat;
import com.cinema.booking.entity.Showtime;
import com.cinema.booking.entity.User;
import jakarta.annotation.Nullable;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Immutable value object for the Specification layer, separate from orchestration
 * {@link com.cinema.booking.services.strategy_decorator.pricing.core.PricingContext}.
 * Used by {@link PricingConditions} without DB access.
 */
@Value
public class PricingSpecificationContext {

    Showtime showtime;

    List<Seat> seats;

    @Nullable User customer;

    @Nullable Promotion promotion;

    BigDecimal fnbTotal;

    int bookedSeatsCount;

    int totalSeatsCount;

    LocalDateTime bookingTime;
}
