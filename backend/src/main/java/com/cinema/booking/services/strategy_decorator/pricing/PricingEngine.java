package com.cinema.booking.services.strategy_decorator.pricing;

import com.cinema.booking.dtos.PriceBreakdownDTO;
import com.cinema.booking.entities.Customer;
import com.cinema.booking.entities.MembershipTier;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Orchestrator của dynamic pricing engine.
 *
 * <p>Flow:
 * <ol>
 *   <li>Strategy layer: ticket + F&amp;B + time-based surcharge (theo {@link PricingLineType})</li>
 *   <li>Decorator chain: NoDiscount → PromotionDiscountDecorator → MemberDiscountDecorator</li>
 *   <li>Populate PriceBreakdownDTO breakdown fields</li>
 * </ol>
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

        BigDecimal membershipDiscount = calculateMembershipDiscount(subtotal, context);

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
                .membershipDiscount(membershipDiscount)
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

        if (hasMemberDiscount(context)) {
            chain = new MemberDiscountDecorator(chain);
        }

        return chain;
    }

    private boolean hasMemberDiscount(PricingContext context) {
        Customer customer = context.getCustomer();
        if (customer == null) return false;
        MembershipTier tier = customer.getTier();
        return tier != null && tier.getDiscountPercent() != null
                && tier.getDiscountPercent().compareTo(BigDecimal.ZERO) > 0;
    }

    private BigDecimal calculateMembershipDiscount(BigDecimal subtotal, PricingContext context) {
        Customer customer = context.getCustomer();
        if (customer == null) return BigDecimal.ZERO;
        MembershipTier tier = customer.getTier();
        if (tier == null || tier.getDiscountPercent() == null
                || tier.getDiscountPercent().compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        return subtotal
                .multiply(tier.getDiscountPercent())
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
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
        if (hasMemberDiscount(context)) {
            parts.add("MEMBER_DISCOUNT");
        }
        if (context.getPromotion() != null) {
            parts.add("PROMO");
        }
        return String.join("+", parts);
    }
}
