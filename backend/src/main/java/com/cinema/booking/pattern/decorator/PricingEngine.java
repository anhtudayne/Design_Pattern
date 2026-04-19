package com.cinema.booking.pattern.decorator;

import com.cinema.booking.dto.PriceBreakdownDTO;
import com.cinema.booking.pattern.proxy.IPricingEngine;
import com.cinema.booking.pattern.strategy.pricing.PricingContext;
import com.cinema.booking.pattern.strategy.pricing.PricingStrategy;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Điều phối ba strategy (vé, F&amp;B, phụ thu thời điểm), sau đó chạy chuỗi giảm giá:
 * {@link NoDiscount} → (có mã) {@link PromotionDiscountDecorator} → (có hạng) {@link MemberDiscountDecorator}.
 */
@Component("pricingEngine")
public class PricingEngine implements IPricingEngine {

    private final PricingStrategy ticketStrategy;
    private final PricingStrategy fnbStrategy;
    private final PricingStrategy timeBasedStrategy;

    public PricingEngine(
            @Qualifier("tuTicketPricingStrategy") PricingStrategy ticketStrategy,
            @Qualifier("tuFnbPricingStrategy") PricingStrategy fnbStrategy,
            @Qualifier("tuTimeBasedPricingStrategy") PricingStrategy timeBasedStrategy) {
        this.ticketStrategy = ticketStrategy;
        this.fnbStrategy = fnbStrategy;
        this.timeBasedStrategy = timeBasedStrategy;
    }

    @Override
    public PriceBreakdownDTO calculateTotalPrice(PricingContext context) {
        BigDecimal ticketTotal = ticketStrategy.calculate(context);
        BigDecimal fnbTotal = fnbStrategy.calculate(context);
        BigDecimal timeBasedSurcharge = timeBasedStrategy.calculate(context);

        BigDecimal subtotal = ticketTotal.add(fnbTotal).add(timeBasedSurcharge);

        DiscountComponent discountChain = buildDiscountChain(context);
        DiscountResult discountResult = discountChain.applyDiscount(subtotal, context);

        BigDecimal finalTotal = subtotal.subtract(discountResult.getTotalDiscount());
        if (finalTotal.compareTo(BigDecimal.ZERO) < 0) {
            finalTotal = BigDecimal.ZERO;
        }

        String appliedStrategy = buildAppliedStrategyDescription(
                ticketTotal, fnbTotal, timeBasedSurcharge, discountResult);

        return PriceBreakdownDTO.builder()
                .ticketTotal(ticketTotal)
                .fnbTotal(fnbTotal)
                .timeBasedSurcharge(timeBasedSurcharge)
                .membershipDiscount(discountResult.getMembershipDiscount())
                .discountAmount(discountResult.getTotalDiscount())
                .appliedStrategy(appliedStrategy)
                .finalTotal(finalTotal)
                .build();
    }

    private DiscountComponent buildDiscountChain(PricingContext context) {
        DiscountComponent chain = new NoDiscount();

        if (context.getPromotion() != null) {
            chain = new PromotionDiscountDecorator(chain, context.getPromotion());
        }

        if (context.getCustomer() != null
                && context.getCustomer().getMembershipDiscountPercent() != null
                && context.getCustomer().getMembershipDiscountPercent().compareTo(BigDecimal.ZERO) > 0) {
            chain = new MemberDiscountDecorator(chain);
        }

        return chain;
    }

    private String buildAppliedStrategyDescription(
            BigDecimal ticketTotal,
            BigDecimal fnbTotal,
            BigDecimal timeBasedSurcharge,
            DiscountResult discountResult) {

        List<String> parts = new ArrayList<>();
        parts.add("Vé: " + ticketTotal);
        parts.add("F&B: " + fnbTotal);
        if (timeBasedSurcharge.compareTo(BigDecimal.ZERO) > 0) {
            parts.add("Phụ thu giờ: +" + timeBasedSurcharge);
        }
        if (!discountResult.getDescription().equals("Không có giảm giá")) {
            parts.add(discountResult.getDescription());
        }
        return String.join(" | ", parts);
    }
}
