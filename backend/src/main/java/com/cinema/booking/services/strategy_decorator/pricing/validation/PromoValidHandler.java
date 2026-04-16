package com.cinema.booking.services.strategy_decorator.pricing.validation;

import com.cinema.booking.entities.Promotion;
import com.cinema.booking.repositories.PromotionRepository;
import com.cinema.booking.services.PromotionInventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Validate promotion nếu promoCode được cung cấp (fail-fast — ném RuntimeException nếu không hợp lệ):
 * <ul>
 *   <li>Promo phải tồn tại trong DB — ném RuntimeException nếu không tìm thấy</li>
 *   <li>Chưa hết hạn ({@code validTo} null hoặc sau thời điểm hiện tại) — ném RuntimeException nếu đã hết hạn</li>
 *   <li>Còn lượt sử dụng (resolved qua {@link PromotionInventoryService}) — ném RuntimeException nếu hết lượt</li>
 * </ul>
 *
 * <p>Nếu không có promoCode trong request, handler bỏ qua validation và delegate sang handler tiếp theo.
 * Nếu promo hợp lệ, {@link PricingValidationContext#promotion} được set với promotion đã resolve.
 */
@Component
@RequiredArgsConstructor
public class PromoValidHandler extends AbstractPricingValidationHandler {

    private final PromotionRepository promotionRepository;
    private final PromotionInventoryService promotionInventoryService;

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

        Promotion availablePromotion = promotionInventoryService.resolvePromotionForPricing(promoCode);
        if (availablePromotion == null) {
            throw new RuntimeException("Mã khuyến mãi '" + promoCode + "' đã hết lượt sử dụng.");
        }

        context.setPromotion(availablePromotion);
    }
}
