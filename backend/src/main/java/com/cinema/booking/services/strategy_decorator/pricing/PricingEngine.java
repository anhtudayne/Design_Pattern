package com.cinema.booking.services.strategy_decorator.pricing;

import com.cinema.booking.dtos.PriceBreakdownDTO;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class PricingEngine {
    private final TicketPricingStrategy ticketStrategy;
    private final FnbPricingStrategy fnbStrategy;

    public PricingEngine(TicketPricingStrategy ticketStrategy, FnbPricingStrategy fnbStrategy) {
        this.ticketStrategy = ticketStrategy;
        this.fnbStrategy = fnbStrategy;
    }

    public PriceBreakdownDTO calculateTotalPrice(PricingContext context) {
        BigDecimal ticketTotal = ticketStrategy.calculate(context);
        BigDecimal fnbTotal = fnbStrategy.calculate(context);
        BigDecimal subtotal = ticketTotal.add(fnbTotal);

        // Build decorator chain
        DiscountComponent discountChain = buildDiscountChain(context);
        DiscountResult discountResult = discountChain.applyDiscount(subtotal, context);

        BigDecimal finalTotal = subtotal.subtract(discountResult.getTotalDiscount());
        BigDecimal discountAmount = discountResult.getTotalDiscount();
        
        if (finalTotal.compareTo(BigDecimal.ZERO) < 0) {
            finalTotal = BigDecimal.ZERO;
            discountAmount = subtotal; // clamp so discount is not more than subtotal
        }

        return PriceBreakdownDTO.builder()
                .ticketTotal(ticketTotal)
                .fnbTotal(fnbTotal)
                .discountAmount(discountAmount)
                .finalTotal(finalTotal)
                .build();
    }

    private DiscountComponent buildDiscountChain(PricingContext context) {
        DiscountComponent chain = new NoDiscount();
        
        if (context.getPromotion() != null) {
            chain = new PromotionDiscountDecorator(chain);
        }
        
        return chain;
    }
}
