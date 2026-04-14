package com.cinema.booking.services.strategy_decorator.pricing.validation;

import com.cinema.booking.entities.Promotion;
import com.cinema.booking.repositories.PromotionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Validate promotion nếu promoCode được cung cấp:
 * <ul>
 *   <li>Promo phải tồn tại trong DB</li>
 *   <li>Chưa hết hạn ({@code validTo} null hoặc sau thời điểm hiện tại)</li>
 *   <li>Còn số lượng ({@code quantity} null hoặc > 0)</li>
 * </ul>
 *
 * <p>Nếu không có promoCode hoặc promo hợp lệ, {@link PricingValidationContext#promotion} = null.
 * Không ném exception khi promoCode không hợp lệ — chỉ bỏ qua (graceful degradation).
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

        boolean hasQuantity = promo.getQuantity() == null || promo.getQuantity() > 0;
        if (!hasQuantity) {
            throw new RuntimeException("Mã khuyến mãi '" + promoCode + "' đã hết lượt sử dụng.");
        }

        context.setPromotion(promo);
    }
}
