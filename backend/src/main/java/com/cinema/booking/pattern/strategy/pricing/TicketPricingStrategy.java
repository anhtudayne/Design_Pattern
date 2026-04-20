package com.cinema.booking.pattern.strategy.pricing;

import com.cinema.booking.entity.Seat;
import com.cinema.booking.entity.Showtime;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;

@Component("tuTicketPricingStrategy")
public class TicketPricingStrategy implements PricingStrategy {

    @Override
    public PricingLineType lineType() {
        return PricingLineType.TICKET;
    }

    @Override
    public BigDecimal calculate(PricingContext context) {
        Showtime showtime = context.getShowtime();
        BigDecimal basePrice = showtime.getBasePrice();

        BigDecimal total = BigDecimal.ZERO;
        for (Seat seat : context.getSeats()) {
            BigDecimal surcharge = (seat.getSeatType() != null && seat.getSeatType().getPriceSurcharge() != null)
                    ? seat.getSeatType().getPriceSurcharge()
                    : BigDecimal.ZERO;
            total = total.add(basePrice).add(surcharge);
        }
        return total;
    }
}
