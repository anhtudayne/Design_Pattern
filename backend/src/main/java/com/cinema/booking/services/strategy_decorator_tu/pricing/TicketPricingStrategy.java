package com.cinema.booking.services.strategy_decorator_tu.pricing;

import com.cinema.booking.entities.Seat;
import com.cinema.booking.entities.Showtime;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Concrete Strategy - Tính tổng tiền vé.
 * Công thức: Σ (basePrice + seatType.priceSurcharge) cho mỗi ghế đã chọn.
 */
@Component("tuTicketPricingStrategy")
public class TicketPricingStrategy implements PricingStrategy {

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
