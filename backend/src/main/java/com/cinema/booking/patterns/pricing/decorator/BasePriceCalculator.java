package com.cinema.booking.patterns.pricing.decorator;

import com.cinema.booking.entities.Seat;
import com.cinema.booking.patterns.pricing.context.PricingContext;
import com.cinema.booking.patterns.pricing.strategy.PricingStrategy;
import com.cinema.booking.patterns.pricing.strategy.PricingStrategySelector;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Concrete Component — KHÔNG extend AbstractPriceCalculatorDecorator.
 * Chọn Strategy phù hợp, tính ticketTotal = sum(adjustedPrice + seatSurcharge) cho từng ghế,
 * rồi tạo PricingAccumulator ban đầu để các Decorator tiếp tục điền vào.
 */
@Service
@RequiredArgsConstructor
public class BasePriceCalculator implements PriceCalculator {

    private final PricingStrategySelector strategySelector;

    @Override
    public PricingAccumulator calculate(PricingContext ctx) {
        PricingStrategy strategy = strategySelector.select(ctx);

        BigDecimal ticketTotal = ctx.getSeats().stream()
                .map(seat -> {
                    BigDecimal adjusted = strategy.adjustBasePrice(
                            ctx.getShowtime().getBasePrice(), ctx);
                    BigDecimal surcharge = hasSurcharge(seat)
                            ? seat.getSeatType().getPriceSurcharge()
                            : BigDecimal.ZERO;
                    return adjusted.add(surcharge);
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        PricingAccumulator acc = new PricingAccumulator();
        acc.setTicketTotal(ticketTotal);
        acc.setAppliedStrategy(strategy.name());
        return acc;
    }

    private boolean hasSurcharge(Seat seat) {
        return seat.getSeatType() != null
                && seat.getSeatType().getPriceSurcharge() != null;
    }
}
