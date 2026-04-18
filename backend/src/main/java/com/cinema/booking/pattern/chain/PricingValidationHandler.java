package com.cinema.booking.pattern.chain;
import com.cinema.booking.pattern.chain.PricingValidationContext;
import com.cinema.booking.pattern.chain.PricingValidationHandler;

/**
 * Chain of Responsibility interface cho pricing validation.
 *
 * <p>Mỗi handler validate một điều kiện trước khi pricing engine chạy.
 * Nếu điều kiện không thỏa, ném RuntimeException dừng chain.
 */
public interface PricingValidationHandler {
    void setNext(PricingValidationHandler next);
    void validate(PricingValidationContext context);
}
