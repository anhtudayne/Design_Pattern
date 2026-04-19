package com.cinema.booking.pattern.strategy.pricing;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Concrete Strategy - Tính tổng tiền F&B (bắp nước).
 * Công thức: Σ (quantity × fnbItem.price) cho mỗi sản phẩm đã chọn.
 */
@Component("tuFnbPricingStrategy")
public class FnbPricingStrategy implements PricingStrategy {

    @Override
    public BigDecimal calculate(PricingContext context) {
        if (context.getFnbItems() == null || context.getFnbItems().isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal total = BigDecimal.ZERO;
        for (PricingContext.FnbItemQuantity fnbOrder : context.getFnbItems()) {
            BigDecimal unitPrice = fnbOrder.getFnbItem().getPrice();
            BigDecimal qty = BigDecimal.valueOf(fnbOrder.getQuantity());
            total = total.add(unitPrice.multiply(qty));
        }
        return total;
    }
}
