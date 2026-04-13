package com.cinema.booking.patterns.pricing.validation;

/**
 * Handler interface cho PriceValidationChain.
 * Contract nhất quán với {@code CheckoutValidationHandler} hiện có
 * (setNext + validate) nhưng hai chain hoàn toàn độc lập về mục đích.
 */
public interface PriceValidationHandler {

    void setNext(PriceValidationHandler next);

    /**
     * Validate context — ném {@link RuntimeException} nếu vi phạm.
     * Nếu không vi phạm, tự động chuyển sang handler kế tiếp.
     */
    void validate(PriceValidationContext ctx);
}
