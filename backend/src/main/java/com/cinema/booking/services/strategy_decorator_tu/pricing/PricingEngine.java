package com.cinema.booking.services.strategy_decorator_tu.pricing;

import com.cinema.booking.dtos.PriceBreakdownDTO;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * PricingEngine — Orchestrator trung tâm.
 * Sử dụng các Strategy để tính giá cấu thành (vé, F&B),
 * sau đó build Decorator chain để áp dụng các lớp giảm giá.
 * Trả về PriceBreakdownDTO hoàn chỉnh.
 */
@Component("tuPricingEngine")
public class PricingEngine {

    private final PricingStrategy ticketStrategy;
    private final PricingStrategy fnbStrategy;

    public PricingEngine(
            @Qualifier("tuTicketPricingStrategy") PricingStrategy ticketStrategy,
            @Qualifier("tuFnbPricingStrategy") PricingStrategy fnbStrategy) {
        this.ticketStrategy = ticketStrategy;
        this.fnbStrategy = fnbStrategy;
    }

    /**
     * Tính toàn bộ giá cho một đơn đặt vé.
     */
    public PriceBreakdownDTO calculateTotalPrice(PricingContext context) {
        // --- Strategy: tính giá cấu thành ---
        BigDecimal ticketTotal = ticketStrategy.calculate(context);
        BigDecimal fnbTotal = fnbStrategy.calculate(context);
        BigDecimal subtotal = ticketTotal.add(fnbTotal);

        // --- Decorator: build discount chain ---
        DiscountComponent discountChain = buildDiscountChain(context);
        DiscountResult discountResult = discountChain.applyDiscount(subtotal, context);

        // --- Tính tổng cuối ---
        BigDecimal finalTotal = subtotal.subtract(discountResult.getTotalDiscount());
        if (finalTotal.compareTo(BigDecimal.ZERO) < 0) {
            finalTotal = BigDecimal.ZERO;
        }

        return PriceBreakdownDTO.builder()
                .ticketTotal(ticketTotal)
                .fnbTotal(fnbTotal)
                .discountAmount(discountResult.getTotalDiscount())
                .finalTotal(finalTotal)
                .build();
    }

    /**
     * Xây dựng chuỗi Decorator giảm giá.
     * Bắt đầu từ NoDiscount (base case), rồi bọc thêm các lớp decorator.
     * Mở rộng tương lai: thêm MembershipDiscountDecorator, HappyHourDiscountDecorator...
     */
    private DiscountComponent buildDiscountChain(PricingContext context) {
        DiscountComponent chain = new NoDiscount();

        // Nếu có mã khuyến mãi → bọc thêm PromotionDiscountDecorator
        if (context.getPromotion() != null) {
            chain = new PromotionDiscountDecorator(chain, context.getPromotion());
        }

        // Mở rộng tương lai:
        // if (membership != null) chain = new MembershipDiscountDecorator(chain, membership);
        // if (isHappyHour())      chain = new HappyHourDiscountDecorator(chain);

        return chain;
    }
}
