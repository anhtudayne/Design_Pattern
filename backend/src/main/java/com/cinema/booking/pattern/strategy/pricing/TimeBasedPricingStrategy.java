package com.cinema.booking.pattern.strategy.pricing;

import com.cinema.booking.pattern.specification.PricingConditions;
import com.cinema.booking.pattern.specification.PricingSpecificationContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Phụ thu cuối tuần / ngày lễ: chỉ tính trên tổng vé, không trên F&amp;B.
 * Ngày lễ được ưu tiên hơn cuối tuần; tỷ lệ lấy từ {@code cinema.pricing.*-surcharge-pct}.
 */
@Component("tuTimeBasedPricingStrategy")
public class TimeBasedPricingStrategy implements PricingStrategy {

    private final BigDecimal weekendSurchargePct;
    private final BigDecimal holidaySurchargePct;

    public TimeBasedPricingStrategy(
            @Value("${cinema.pricing.weekend-surcharge-pct:15}") BigDecimal weekendSurchargePct,
            @Value("${cinema.pricing.holiday-surcharge-pct:20}") BigDecimal holidaySurchargePct) {
        this.weekendSurchargePct = weekendSurchargePct;
        this.holidaySurchargePct = holidaySurchargePct;
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

        BigDecimal ticketTotal = computeTicketTotal(context);
        if (ticketTotal.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        PricingSpecificationContext specCtx = buildSpecContext(context);

        BigDecimal pct;
        if (PricingConditions.isHoliday().test(specCtx)) {
            pct = holidaySurchargePct;
        } else if (PricingConditions.isWeekend().test(specCtx)) {
            pct = weekendSurchargePct;
        } else {
            return BigDecimal.ZERO;
        }

        return ticketTotal
                .multiply(pct)
                .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP);
    }

    private BigDecimal computeTicketTotal(PricingContext context) {
        BigDecimal basePrice = context.getShowtime().getBasePrice();
        BigDecimal total = BigDecimal.ZERO;
        for (var seat : context.getSeats()) {
            BigDecimal surcharge = (seat.getSeatType() != null && seat.getSeatType().getPriceSurcharge() != null)
                    ? seat.getSeatType().getPriceSurcharge()
                    : BigDecimal.ZERO;
            total = total.add(basePrice).add(surcharge);
        }
        return total;
    }

    private PricingSpecificationContext buildSpecContext(PricingContext ctx) {
        return new PricingSpecificationContext(
                ctx.getShowtime(),
                ctx.getSeats(),
                ctx.getCustomer(),
                ctx.getPromotion(),
                BigDecimal.ZERO,
                ctx.getBookedSeatsCount(),
                ctx.getTotalSeatsCount(),
                ctx.getBookingTime()
        );
    }
}
