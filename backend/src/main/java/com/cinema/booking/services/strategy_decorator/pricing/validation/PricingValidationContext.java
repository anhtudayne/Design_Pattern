package com.cinema.booking.services.strategy_decorator.pricing.validation;

import com.cinema.booking.dtos.BookingCalculationDTO;
import com.cinema.booking.entities.Promotion;
import com.cinema.booking.entities.Showtime;
import lombok.Builder;
import lombok.Data;

/**
 * Context truyền qua CoR pricing validation chain.
 *
 * <p>Các handler populate {@code showtime} và {@code promotion} trong quá trình validate.
 * BookingServiceImpl tái dùng các field này để build PricingContext — tránh load lại DB.
 */
@Data
@Builder
public class PricingValidationContext {
    /** Request gốc từ client. */
    private BookingCalculationDTO request;

    /** Populated bởi ShowtimeFutureHandler sau khi load và validate. */
    private Showtime showtime;

    /**
     * Populated bởi PromoValidHandler nếu promoCode hợp lệ.
     * Null nếu không có promoCode hoặc promo không hợp lệ.
     */
    private Promotion promotion;
}
