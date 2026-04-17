package com.cinema.booking.services.strategy_decorator.pricing.strategy;

import com.cinema.booking.entities.Seat;
import com.cinema.booking.services.strategy_decorator.pricing.core.PricingContext;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class TicketPricingStrategy implements PricingStrategy {

    @Override
    public PricingLineType lineType() {
        return PricingLineType.TICKET;
    }

    @Override
    public BigDecimal calculate(PricingContext context) {
        if (context.getSeats() == null || context.getShowtime() == null) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal ticketTotal = BigDecimal.ZERO;
        BigDecimal basePrice = context.getShowtime().getBasePrice();
        
        for (Seat seat : context.getSeats()) {
            BigDecimal seatSurcharge = (seat.getSeatType() != null && seat.getSeatType().getPriceSurcharge() != null)
                    ? seat.getSeatType().getPriceSurcharge()
                    : BigDecimal.ZERO;
            ticketTotal = ticketTotal.add(basePrice.add(seatSurcharge));
        }
        return ticketTotal;
    }
}
