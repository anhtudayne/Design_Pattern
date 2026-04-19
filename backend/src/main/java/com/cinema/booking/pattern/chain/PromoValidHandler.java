package com.cinema.booking.pattern.chain;

import com.cinema.booking.entity.Promotion;
import com.cinema.booking.repository.PromotionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Validate promotion nếu promoCode được cung cấp.
 */
@Component
@RequiredArgsConstructor
public class PromoValidHandler extends AbstractPricingValidationHandler {

    private final PromotionRepository promotionRepository;

    @Override
    protected void doValidate(PricingValidationContext context) {
        String promoCode = context.getRequest().getPromoCode();
        if (promoCode == null || promoCode.isBlank()) {
            return;
        }

        Promotion promo = promotionRepository.findByCode(promoCode).orElse(null);
        if (promo == null) {
            throw new RuntimeException("Mã khuyến mãi '" + promoCode + "' không tồn tại.");
        }

        boolean notExpired = promo.getValidTo() == null || LocalDateTime.now().isBefore(promo.getValidTo());
        if (!notExpired) {
            throw new RuntimeException("Mã khuyến mãi '" + promoCode + "' đã hết hạn.");
        }

        // Inventory check removed per Phase 1/Phase 3 clean up. 
        // Logic should be moved to Booking service if needed, but for compilation we keep it simple.
        
        context.setPromotion(promo);
    }
}
