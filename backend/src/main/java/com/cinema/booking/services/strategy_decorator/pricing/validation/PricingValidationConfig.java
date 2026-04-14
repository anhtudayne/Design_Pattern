package com.cinema.booking.services.strategy_decorator.pricing.validation;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Nối chain: ShowtimeFuture → SeatsAvailable → PromoValid.
 *
 * <p>Thứ tự ý nghĩa:
 * <ol>
 *   <li>ShowtimeFutureHandler — load showtime, validate chưa chiếu</li>
 *   <li>SeatsAvailableHandler — validate ghế chưa bán (cần showtimeId từ request)</li>
 *   <li>PromoValidHandler    — validate promo if present (reads {@code request.promoCode} only, not showtime from context)</li>
 * </ol>
 */
@Configuration
public class PricingValidationConfig {

    @Bean("pricingValidationChain")
    public PricingValidationHandler pricingValidationChain(
            ShowtimeFutureHandler showtimeFutureHandler,
            SeatsAvailableHandler seatsAvailableHandler,
            PromoValidHandler promoValidHandler) {

        showtimeFutureHandler.setNext(seatsAvailableHandler);
        seatsAvailableHandler.setNext(promoValidHandler);

        return showtimeFutureHandler;
    }
}
