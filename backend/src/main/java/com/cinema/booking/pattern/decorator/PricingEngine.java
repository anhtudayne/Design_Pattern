package com.cinema.booking.pattern.decorator;

import com.cinema.booking.dto.PriceBreakdownDTO;
import com.cinema.booking.pattern.proxy.IPricingEngine;
import com.cinema.booking.pattern.strategy.pricing.PricingContext;
import com.cinema.booking.pattern.strategy.pricing.PricingLineType;
import com.cinema.booking.pattern.strategy.pricing.PricingStrategy;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Điều phối ba strategy (vé, F&amp;B, phụ thu thời điểm), sau đó chạy chuỗi giảm giá:
 * {@link NoDiscount} → (có mã) {@link PromotionDiscountDecorator} → (có hạng) {@link MemberDiscountDecorator}.
 */
@Component("pricingEngine")
public class PricingEngine implements IPricingEngine {

    private final Map<PricingLineType, PricingStrategy> strategiesByLine;

    public PricingEngine(List<PricingStrategy> pricingStrategies) {
        this.strategiesByLine = new EnumMap<>(PricingLineType.class);
        for (PricingStrategy strategy : pricingStrategies) {
            strategiesByLine.put(strategy.lineType(), strategy);
        }
        // Validate all strategies are registered
        for (PricingLineType type : PricingLineType.values()) {
            if (!strategiesByLine.containsKey(type)) {
                throw new IllegalStateException(
                    "Missing PricingStrategy for line type: " + type);
            }
        }
    }

    @Override
    public PriceBreakdownDTO calculateTotalPrice(PricingContext context) {
        BigDecimal ticketTotal = strategiesByLine.get(PricingLineType.TICKET).calculate(context);
        BigDecimal fnbTotal = strategiesByLine.get(PricingLineType.FNB).calculate(context);
        BigDecimal timeBasedSurcharge = strategiesByLine.get(PricingLineType.TIME_BASED_SURCHARGE).calculate(context);

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
