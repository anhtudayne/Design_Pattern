package com.cinema.booking.services.impl;

import com.cinema.booking.dtos.BookingCalculationDTO;
import com.cinema.booking.dtos.PriceBreakdownDTO;
import com.cinema.booking.patterns.pricing.context.PricingContext;
import com.cinema.booking.patterns.pricing.context.PricingContextFactory;
import com.cinema.booking.patterns.pricing.decorator.PriceCalculator;
import com.cinema.booking.patterns.pricing.decorator.PriceCalculatorChainFactory;
import com.cinema.booking.patterns.pricing.decorator.PricingAccumulator;
import com.cinema.booking.patterns.pricing.validation.PriceValidationContext;
import com.cinema.booking.patterns.pricing.validation.PriceValidationHandler;
import com.cinema.booking.services.DynamicPricingService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * Orchestrator — nối toàn bộ pattern layer thành một luồng 4 bước.
 * Không chứa bất kỳ BigDecimal logic hay business rule gia nào trực tiếp.
 *
 * <p>Dependency graph (không có circular):
 * <pre>
 * DynamicPricingServiceImpl
 *   ├── PricingContextFactory        (Phase 0 — load DB + build context)
 *   ├── PriceCalculatorChainFactory  (Phase 2 — Decorator chain)
 *   └── PriceValidationHandler       (Phase 3 — CoR validation)
 * </pre>
 * </p>
 *
 * <p>Bean name {@code "dynamicPricingServiceImpl"} — Phase 5 sẽ thêm
 * {@code CachingDynamicPricingProxy @Primary} wrap service này.</p>
 */
@Service("dynamicPricingServiceImpl")
@RequiredArgsConstructor
public class DynamicPricingServiceImpl implements DynamicPricingService {

    private final PricingContextFactory contextFactory;
    private final PriceCalculatorChainFactory chainFactory;

    @Qualifier("priceValidationChain")
    private final PriceValidationHandler validationChain;

    @Override
    public PriceBreakdownDTO calculatePrice(BookingCalculationDTO request) {
        // 1. Load dữ liệu từ DB và build context (SRP: factory xử lý)
        PricingContext ctx = contextFactory.build(request);

        // 2. Tính giá qua Decorator chain (Strategy + 4 Decorator)
        PriceCalculator chain = chainFactory.buildChain();
        PricingAccumulator acc = chain.calculate(ctx);

        // 3. Validate kết quả qua Chain of Responsibility
        validationChain.validate(toValidationContext(acc, ctx));

        // 4. Convert sang DTO và return
        return acc.toDTO();
    }

    private PriceValidationContext toValidationContext(PricingAccumulator acc, PricingContext ctx) {
        return new PriceValidationContext(
                acc.subtotal(),
                acc.totalDiscount(),
                acc.finalTotal(),
                ctx.getSeats().size()
        );
    }
}
