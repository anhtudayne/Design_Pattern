package com.cinema.booking.services.strategy_decorator.pricing.core;

import com.cinema.booking.dtos.PriceBreakdownDTO;
import com.cinema.booking.services.strategy_decorator.pricing.decorator.DiscountComponent;
import com.cinema.booking.services.strategy_decorator.pricing.decorator.DiscountResult;
import com.cinema.booking.services.strategy_decorator.pricing.decorator.NoDiscount;
import com.cinema.booking.services.strategy_decorator.pricing.decorator.PromotionDiscountDecorator;
import com.cinema.booking.services.strategy_decorator.pricing.proxy.IPricingEngine;
import com.cinema.booking.services.strategy_decorator.pricing.strategy.PricingLineType;
import com.cinema.booking.services.strategy_decorator.pricing.strategy.PricingStrategy;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Orchestrator của dynamic pricing engine.
 */
@Component("pricingEngine")
public class PricingEngine implements IPricingEngine {

    private final Map<PricingLineType, PricingStrategy> strategiesByLine;
    private final NoDiscount noDiscount;

    public PricingEngine(List<PricingStrategy> strategies, NoDiscount noDiscount) {
        this.noDiscount = noDiscount;
        this.strategiesByLine = new EnumMap<>(PricingLineType.class);
        for (PricingStrategy s : strategies) {
            if (strategiesByLine.put(s.lineType(), s) != null) {
                throw new IllegalStateException("Duplicate PricingLineType: " + s.lineType());
            }
        }
        for (PricingLineType line : PricingLineType.values()) {
            if (!strategiesByLine.containsKey(line)) {
                throw new IllegalStateException("Missing PricingStrategy for line: " + line);
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

        BigDecimal totalDiscount = discountResult.getTotalDiscount();
        BigDecimal finalTotal = subtotal.subtract(totalDiscount);
        if (finalTotal.compareTo(BigDecimal.ZERO) < 0) {
            finalTotal = BigDecimal.ZERO;
            totalDiscount = subtotal;
        }

        String appliedStrategy = buildAppliedStrategyLabel(context, timeBasedSurcharge);

        return PriceBreakdownDTO.builder()
                .ticketTotal(ticketTotal)
                .fnbTotal(fnbTotal)
                .timeBasedSurcharge(timeBasedSurcharge)
                .discountAmount(totalDiscount)
                .appliedStrategy(appliedStrategy)
                .finalTotal(finalTotal)
                .build();
    }

    private DiscountComponent buildDiscountChain(PricingContext context) {
        DiscountComponent chain = noDiscount;

        if (context.getPromotion() != null) {
            chain = new PromotionDiscountDecorator(chain);
        }

        // Membership discount removed as MembershipTier entity was removed in refactoring.

        return chain;
    }

    private String buildAppliedStrategyLabel(PricingContext context, BigDecimal timeBasedSurcharge) {
        List<String> parts = new ArrayList<>();
        parts.add("TICKET");
        if (context.getResolvedFnbs() != null && !context.getResolvedFnbs().isEmpty()) {
            parts.add("FNB");
        }
        if (timeBasedSurcharge.compareTo(BigDecimal.ZERO) > 0) {
            parts.add("TIME_BASED");
        }
        if (context.getPromotion() != null) {
            parts.add("PROMO");
        }
        return String.join("+", parts);
    }
}
