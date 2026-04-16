package com.cinema.booking.services.strategy_decorator.pricing.strategy;

import com.cinema.booking.services.strategy_decorator.pricing.core.PricingContext;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Tính tổng tiền F&B từ danh sách ResolvedFnbItem đã được load giá từ DB
 * tại tầng service trước khi vào engine. Không truy cập DB trực tiếp.
 */
@Component
public class FnbPricingStrategy implements PricingStrategy {

    @Override
    public PricingLineType lineType() {
        return PricingLineType.FNB;
    }

    @Override
    public BigDecimal calculate(PricingContext context) {
        if (context.getResolvedFnbs() == null || context.getResolvedFnbs().isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal fnbTotal = BigDecimal.ZERO;
        for (PricingContext.ResolvedFnbItem item : context.getResolvedFnbs()) {
            BigDecimal line = item.price().multiply(BigDecimal.valueOf(item.quantity()));
            fnbTotal = fnbTotal.add(line);
        }
        return fnbTotal;
    }
}
