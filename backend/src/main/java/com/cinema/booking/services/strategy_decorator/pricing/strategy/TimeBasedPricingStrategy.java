package com.cinema.booking.services.strategy_decorator.pricing.strategy;

import com.cinema.booking.services.strategy_decorator.pricing.specification.PricingConditions;
import com.cinema.booking.services.strategy_decorator.pricing.specification.PricingSpecificationContext;
import com.cinema.booking.services.strategy_decorator.pricing.core.PricingContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Time-based surcharge (weekend / holiday) using Specification predicates from {@link PricingConditions}.
 */
@Component
public class TimeBasedPricingStrategy implements PricingStrategy {

    private final BigDecimal weekendSurchargePct;
    private final BigDecimal holidaySurchargePct;

    public TimeBasedPricingStrategy(
            @Value("${cinema.pricing.weekend-surcharge-pct:15}") int weekendSurchargePct,
            @Value("${cinema.pricing.holiday-surcharge-pct:20}") int holidaySurchargePct) {
        this.weekendSurchargePct = BigDecimal.valueOf(weekendSurchargePct);
        this.holidaySurchargePct = BigDecimal.valueOf(holidaySurchargePct);
    }

    @Override
    public PricingLineType lineType() {
        return PricingLineType.TIME_BASED_SURCHARGE;
    }

    @Override
    public BigDecimal calculate(PricingContext context) {
        if (context.getShowtime() == null || context.getSeats() == null) {
            return BigDecimal.ZERO;
        }

        PricingSpecificationContext specCtx = toSpecContext(context);

        Predicate<PricingSpecificationContext> isHoliday = PricingConditions.isHoliday();
        Predicate<Predicate<PricingSpecificationContext>> isWeekendWrapper = p -> PricingConditions.isWeekend().test(specCtx); // Simplified for now
        
        boolean holidayMatch = isHoliday.test(specCtx);
        boolean weekendMatch = PricingConditions.isWeekend().test(specCtx);

        if (!holidayMatch && !weekendMatch) {
            return BigDecimal.ZERO;
        }

        BigDecimal appliedRate = holidayMatch ? holidaySurchargePct : weekendSurchargePct;

        BigDecimal ticketSubtotal = BigDecimal.ZERO;
        
        // Note: although showtime.basePrice was marked for deletion in class diagram, 
        // it was kept in Phase 1 to support this calculation logic.
        BigDecimal basePrice = context.getShowtime().getBasePrice();
        for (com.cinema.booking.entities.Seat seat : context.getSeats()) {
            BigDecimal seatSurcharge = (seat.getSeatType() != null && seat.getSeatType().getPriceSurcharge() != null)
                    ? seat.getSeatType().getPriceSurcharge()
                    : BigDecimal.ZERO;
            ticketSubtotal = ticketSubtotal.add(basePrice.add(seatSurcharge));
        }

        return ticketSubtotal
                .multiply(appliedRate)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    private PricingSpecificationContext toSpecContext(PricingContext ctx) {
        BigDecimal fnbTotal = ctx.getResolvedFnbs() == null ? BigDecimal.ZERO
                : ctx.getResolvedFnbs().stream()
                        .map(item -> item.price().multiply(BigDecimal.valueOf(item.quantity())))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<com.cinema.booking.entities.Seat> seats = ctx.getSeats() != null
                ? ctx.getSeats() : new ArrayList<>();

        return new PricingSpecificationContext(
                ctx.getShowtime(),
                seats,
                ctx.getUser(),
                ctx.getPromotion(),
                fnbTotal,
                ctx.getBookedSeatsCount(),
                ctx.getTotalSeatsCount(),
                ctx.getBookingTime()
        );
    }
}
