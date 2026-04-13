package com.cinema.booking.patterns.pricing.validation;

/**
 * Abstract base — template method pattern:
 * gọi {@link #doValidate(PriceValidationContext)} trước,
 * nếu không throw thì chuyển sang handler kế tiếp.
 * Concrete handler chỉ cần implement {@code doValidate()} (3-5 dòng).
 */
public abstract class AbstractPriceValidationHandler implements PriceValidationHandler {

    private PriceValidationHandler next;

    @Override
    public void setNext(PriceValidationHandler next) {
        this.next = next;
    }

    @Override
    public void validate(PriceValidationContext ctx) {
        doValidate(ctx);                               // nếu throw thì chain dừng tại đây
        if (next != null) next.validate(ctx);
    }

    protected abstract void doValidate(PriceValidationContext ctx);
}
