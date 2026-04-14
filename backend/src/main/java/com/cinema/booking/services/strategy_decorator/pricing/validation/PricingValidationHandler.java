package com.cinema.booking.services.strategy_decorator.pricing.validation;

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
