package com.cinema.booking.services.strategy_decorator.pricing;

import com.cinema.booking.dtos.BookingCalculationDTO;
import com.cinema.booking.entities.FnbItem;
import com.cinema.booking.repositories.FnbItemRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class FnbPricingStrategy implements PricingStrategy {

    private final FnbItemRepository fnbItemRepository;

    public FnbPricingStrategy(FnbItemRepository fnbItemRepository) {
        this.fnbItemRepository = fnbItemRepository;
    }

    @Override
    public BigDecimal calculate(PricingContext context) {
        if (context.getFnbs() == null || context.getFnbs().isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal fnbTotal = BigDecimal.ZERO;
        for (BookingCalculationDTO.FnbOrderDTO fnbOrder : context.getFnbs()) {
            FnbItem item = fnbItemRepository.findById(fnbOrder.getItemId())
                    .orElseThrow(() -> new RuntimeException("Sản phẩm F&B không tồn tại!"));
            BigDecimal line = item.getPrice().multiply(BigDecimal.valueOf(fnbOrder.getQuantity()));
            fnbTotal = fnbTotal.add(line);
        }
        return fnbTotal;
    }
}
